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
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.RequestGeneratorContext;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.NotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.Bundle;
import org.hl7.fhir.r4b.model.CodeableConcept;
import org.hl7.fhir.r4b.model.OperationOutcome;
import org.hl7.fhir.r4b.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class TransactionProvider {
    private static final Logger log = LoggerFactory.getLogger(TransactionProvider.class);

    private final ObjectProvider<Resolver> resolverObjectProvider;
    private final ObjectProvider<RequestGeneratorContext> requestGeneratorContextObjectProvider;
    private final Config config;
    private final MapperRegistry mapperRegistry;

    public TransactionProvider(ObjectProvider<Resolver> resolverObjectProvider, ObjectProvider<RequestGeneratorContext> requestGeneratorContextObjectProvider, Config config, MapperRegistry mapperRegistry) {
        this.resolverObjectProvider = resolverObjectProvider;
        this.requestGeneratorContextObjectProvider = requestGeneratorContextObjectProvider;
        this.config = config;
        this.mapperRegistry = mapperRegistry;
    }

    private Bundle handleBatch(Bundle batch) {
        throw new NotImplementedOperationException("handling of batch transactions");
    }

    private <A, B> A toPheno(B resource, Class<A> target) throws Exception {
        log.info("Translating: {}", target);
        PhenoMapper<A, B> mapper = mapperRegistry.getPhenoMapper(target, (Class<B>) resource.getClass());
        return mapper.toPheno(resource);
    }

    private Bundle handleTransaction(Bundle transaction) throws Exception {
        Resolver resolver = resolverObjectProvider.getObject();
        RequestGeneratorContext ctx = requestGeneratorContextObjectProvider.getObject();
        for(var entry : transaction.getEntry()) {
//            log.info("Entry {}", entry.getFullUrl());
//            log.info("{} {}", entry.getRequest().getMethod(), entry.getRequest().getUrl());
            Resource resource = entry.getResource();
            resolver.register(entry.getFullUrl(), resource);
            if (entry.getRequest().getMethod() != Bundle.HTTPVerb.POST) {
                throw new NotImplementedOperationException(entry.getRequest().getMethod().toString());
            }
            Mapping mapping = config.getMappings().get(resource.getResourceType().toString());
            if(mapping == null) continue;
            if (mapping.getProfile() != null && resource.getMeta().getProfile().stream().noneMatch(p -> p.getValueAsString().equals(mapping.getProfile()))) {
                var oo = new OperationOutcome();
                oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setCode(OperationOutcome.IssueType.STRUCTURE).setDetails(new CodeableConcept().setText("Resource didn't include the correct profile"));
                throw new UnprocessableEntityException("Profile does not match", oo);
            }
        }
        for(var entry : transaction.getEntry()) {
            Resource resource = entry.getResource();
            Mapping mapping = config.getMappings().get(resource.getResourceType().toString());
            if(mapping == null) continue;
            Class<?> target;
            try {
                target = getClass().getClassLoader().loadClass(mapping.getTarget());
            } catch (ClassNotFoundException e) {
                throw new InternalErrorException(e);
            }
            ctx.setCurrentResource(resource);
            if(!target.isAnnotationPresent(Owned.class)) {
                Object pheno = toPheno(resource, target);
                log.info("Converted: {}", pheno);
            }
            ctx.setCurrentResource(null);

        }
        throw new NotImplementedOperationException("Not implemented");
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
