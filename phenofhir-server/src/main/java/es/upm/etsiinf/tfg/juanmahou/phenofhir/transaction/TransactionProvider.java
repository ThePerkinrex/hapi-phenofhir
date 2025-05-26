package es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction;

import ca.uhn.fhir.rest.annotation.Transaction;
import ca.uhn.fhir.rest.annotation.TransactionParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.exceptions.InternalErrorException;
import ca.uhn.fhir.rest.server.exceptions.InvalidRequestException;
import ca.uhn.fhir.rest.server.exceptions.NotImplementedOperationException;
import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import es.upm.etsiinf.tfg.juanmahou.entities.Owned;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRunner;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.Bundle;
import org.hl7.fhir.r4b.model.CodeableConcept;
import org.hl7.fhir.r4b.model.OperationOutcome;
import org.hl7.fhir.r4b.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
public class TransactionProvider {
    private static final Logger log = LoggerFactory.getLogger(TransactionProvider.class);

    private final ObjectProvider<Resolver> resolverObjectProvider;
    private final Config config;
    private final MapperRegistry mapperRegistry;

    public TransactionProvider(ObjectProvider<Resolver> resolverObjectProvider, Config config, MapperRegistry mapperRegistry) {
        this.resolverObjectProvider = resolverObjectProvider;
        this.config = config;
        this.mapperRegistry = mapperRegistry;
    }

    private Bundle handleBatch(Bundle batch) {
        throw new NotImplementedOperationException("handling of batch transactions");
    }

    private Bundle.BundleEntryComponent processEntry(String path, Resolver.Resolved resolved) {
        Bundle.BundleEntryComponent response = new Bundle.BundleEntryComponent();
        List<MapperRegistry.MapperAndData> mappers = mapperRegistry
                .getAllForArgs(List.of(ResolvableType.forInstance(resolved.getResource())))
                .filter(m -> !m.key().ret().as(WithId.class).equalsType(ResolvableType.NONE))
                .toList();
        if(mappers.isEmpty()) {
            log.warn("No mapper found for {}", resolved);
            response.getResponse().setStatus("404");
            return response;
        } else if (mappers.size() > 1) {
            log.warn("Too many mappers found for {}", resolved);
            response.getResponse().setStatus("500");
            return response;
        }
        MapperRunner mapper = mappers.getFirst().runner();
        WithId<?> result;
        try {
            result = (WithId<?>) mapper.run(List.of(resolved.getResource()));
        } catch (Exception e) {
            log.error("Error handling {}", resolved, e);
            response.getResponse().setStatus("500");
            return response;
        }

        resolved.setPheno(result);

        response.getResponse().setStatus("201");
        response.getResponse().setLocation(path + "/" + result.getId().toString());

        return response;
    }

    private Bundle handleTransaction(Bundle transaction) throws Exception {
        Resolver resolver = resolverObjectProvider.getObject();
        Bundle responseBundle = new Bundle();
        responseBundle.setType(Bundle.BundleType.TRANSACTIONRESPONSE);
        for(var entry : transaction.getEntry()) {
            Resource resource = entry.getResource();
            Resolver.Resolved resolved = resolver.register(entry.getFullUrl(), resource);
            if (entry.getRequest().getMethod() != Bundle.HTTPVerb.POST) {
                throw new NotImplementedOperationException(entry.getRequest().getMethod().toString());
            }
            responseBundle.addEntry(processEntry(entry.getRequest().getUrl(), resolved));
        }
//        for(var entry : transaction.getEntry()) {
//            Resource resource = entry.getResource();
//            Mapping mapping = config.getMappings().get(resource.getResourceType().toString());
//            if(mapping == null) continue;
//            Class<?> target;
//            try {
//                target = getClass().getClassLoader().loadClass(mapping.getTarget());
//            } catch (ClassNotFoundException e) {
//                throw new InternalErrorException(e);
//            }
////            ctx.setCurrentResource(resource);
//            if(!target.isAnnotationPresent(Owned.class)) {
//                Object pheno = toPheno(entry.getFullUrl(), target, resolver);
//                log.info("Converted: {}", pheno);
//            }
////            ctx.setCurrentResource(null);
//        }
//        Bundle responseBundle = new Bundle();
//        responseBundle.setType(Bundle.BundleType.TRANSACTIONRESPONSE);
//        for(var entry : transaction.getEntry()) {
//            Resolver.Resolved resolved = resolver.get(entry.getFullUrl());
//            Bundle.BundleEntryComponent responseEntry = responseBundle.addEntry();
//            if(resolved.getPheno() instanceof WithId<?> id) {
//                responseEntry.getResponse().setStatus("200");
//                responseEntry.getResponse().setLocation(entry.getRequest().getUrl() + "/" + id.getId());
//            }else{
//                responseEntry.getResponse().setStatus("400");
//            }
//        }
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
