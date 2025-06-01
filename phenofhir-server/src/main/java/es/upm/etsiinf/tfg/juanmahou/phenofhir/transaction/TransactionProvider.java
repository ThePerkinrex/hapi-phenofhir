package es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction;

import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import es.upm.etsiinf.tfg.juanmahou.entities.Owned;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRunner;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.CurieRegistryManager;
import org.hl7.fhir.r4.model.Bundle;
import org.hl7.fhir.r4.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class TransactionProvider {
    private static final Logger log = LoggerFactory.getLogger(TransactionProvider.class);

    private final ObjectProvider<Resolver> resolverObjectProvider;
    private final ObjectProvider<CurieRegistryManager> curieRegistryManager;
    private final Config config;
    private final MapperRegistry mapperRegistry;

    public TransactionProvider(ObjectProvider<Resolver> resolverObjectProvider, ObjectProvider<CurieRegistryManager> curieRegistryManager, Config config, MapperRegistry mapperRegistry) {
        this.resolverObjectProvider = resolverObjectProvider;
        this.curieRegistryManager = curieRegistryManager;
        this.config = config;
        this.mapperRegistry = mapperRegistry;
    }

    private Bundle handleBatch(Bundle batch) {
        throw new NotImplementedOperationException("handling of batch transactions");
    }

    private static class ResolvableBundleEntryComponent {
        private Bundle.BundleEntryComponent entry;
        private final String id;
        private final String path;

        public ResolvableBundleEntryComponent(String id, String path) {
            this.id = id;
            this.path = path;
            entry = null;
        }

        public Bundle.BundleEntryComponent getEntry() {
            if(entry == null) entry = new Bundle.BundleEntryComponent();
            return entry;
        }

        public Bundle.BundleEntryComponent resolve(Resolver resolver) {
            if(entry != null) return entry;

            WithId<?> res = resolver.get(id).getPheno();
            if(res == null) {
                log.error("Bundle entry for {} wasn't resolved", id);
                getEntry().getResponse().setStatus("500");
            }else{
                getEntry().getResponse().setStatus("201");
                getEntry().getResponse().setLocation(path + "/" + res.getId().toString());
            }
            return getEntry();
        }

        public String getId() {
            return id;
        }

        public String getPath() {
            return path;
        }
    }

    private void processEntry(Resolver.Resolved resolved, ResolvableBundleEntryComponent response) {
        List<MapperRegistry.MapperAndData> mappers = mapperRegistry
                .getAllForArgs(List.of(ResolvableType.forInstance(resolved.getResource())))
                .filter(m -> !m.key().ret().as(WithId.class).equalsType(ResolvableType.NONE))
                .toList();
        if(mappers.isEmpty()) {
            log.warn("No mapper found for {}", resolved);
            response.getEntry().getResponse().setStatus("404");
            return;
        } else if (mappers.size() > 1) {
            log.warn("Too many mappers found for {}", resolved);
            response.getEntry().getResponse().setStatus("500");
            return;
        }
        var mapperData = mappers.getFirst();
        if(mapperData.key().ret().toClass().isAnnotationPresent(Owned.class)) {
            log.warn("Skipping {}, result {} is owned", resolved.getResource(), mapperData.key().ret());
            return;
        }
        MapperRunner mapper = mapperData.runner();

        try {
            CurieRegistryManager curieRegistryManager = this.curieRegistryManager.getObject();
            curieRegistryManager.newRegistry();
            mapper.run(List.of(resolved.getResource()), x -> {
                WithId<?> result = (WithId<?>) x;
                resolved.setPheno(result);

                response.getEntry().getResponse().setStatus("201");
                response.getEntry().getResponse().setLocation(response.getPath() + "/" + result.getId().toString());
                curieRegistryManager.clearRegistry();
            });
        } catch (Exception e) {
            log.error("Error handling {}", resolved, e);
            response.getEntry().getResponse().setStatus("500");
        }



    }

    private Bundle handleTransaction(Bundle transaction) throws Exception {
        Resolver resolver = resolverObjectProvider.getObject();
        List<ResolvableBundleEntryComponent> entries = new ArrayList<>(transaction.getEntry().size());
        for(var entry : transaction.getEntry()) {
            Resource resource = entry.getResource();
            resolver.register(entry.getFullUrl(), resource);
            if (entry.getRequest().getMethod() != Bundle.HTTPVerb.POST) {
                throw new NotImplementedOperationException(entry.getRequest().getMethod().toString());
            }
            entries.add(new ResolvableBundleEntryComponent(entry.getFullUrl(), entry.getRequest().getUrl()));
//            responseBundle.addEntry(processEntry(entry.getRequest().getUrl(), resolved));
        }
        for(var entry : entries) {
            processEntry(resolver.get(entry.getId()), entry);
        }
        Bundle responseBundle = new Bundle();
        responseBundle.setType(Bundle.BundleType.TRANSACTIONRESPONSE);
        for(var entry : entries) {
           responseBundle.addEntry(entry.resolve(resolver));
        }

        return responseBundle;
//        throw new RuntimeException("Unimplemented");
    }

    @Transaction
    @Transactional
    public Bundle transaction(@TransactionParam Bundle transaction) throws Exception {
        return switch (transaction.getType()) {
            case BATCH -> handleBatch(transaction);
            case TRANSACTION -> handleTransaction(transaction);
            default -> throw new InvalidRequestException("Bundle of type " + transaction.getType() + " is invalid");
        };
    }
}
