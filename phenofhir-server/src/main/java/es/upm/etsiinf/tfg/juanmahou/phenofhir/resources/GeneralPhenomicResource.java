package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRunner;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.KeyUtils;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.IdType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.naming.OperationNotSupportedException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GeneralPhenomicResource<PhenoKey extends Id, Pheno extends WithId<PhenoKey>> implements IResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(GeneralPhenomicResource.class);
    private final Class<? extends IBaseResource> resource;
    private final ResolvableType target;
    private final MapperRunner resourceMapping;
    private final MapperRunner idMapper;
    private final CrudRepository<Pheno, PhenoKey> repository;


    public GeneralPhenomicResource(
            ResolvableType target,
            ResolvableType resource,
            MapperRunner mapper,
            RepositoryProvider repositoryProvider,
            MapperRegistry registry,
            KeyUtils keyUtils
    ) {
        if(!ResolvableType.forClass(IBaseResource.class).isAssignableFrom(resource)) throw new RuntimeException("Cant create resource provider for " + resource);
        this.resource = (Class<? extends IBaseResource>) resource.toClass();
        this.target = target;
        this.resourceMapping = mapper;
        this.idMapper = registry.getMapper(keyUtils.getKeyType(target), List.of(ResolvableType.forClass(IdType.class)));
        if(this.idMapper == null) throw new RuntimeException("No id mapper found for " + keyUtils.getKeyType(target));
        this.repository = repositoryProvider.getCrudRepository(target);
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return resource;
    }

//    @Create
//    @Transactional
//    public MethodOutcome create(@ResourceParam FHIR resource) throws Exception {
//        if (resource.getMeta().getProfile().stream().noneMatch(p -> p.getValueAsString().equals(mapping.getProfile()))) {
//            MethodOutcome outcome = new MethodOutcome();
//            outcome.setCreated(false);
//            var oo = new OperationOutcome();
//            oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setCode(OperationOutcome.IssueType.STRUCTURE).setDetails(new CodeableConcept().setText("Resource didn't include the correct profile"));
//            outcome.setOperationOutcome(oo);
//            return outcome;
//        }
//
//        WithId<? extends Id> pheno = resourceMapping.toPheno(resource);
//        log.info("Created {}", pheno);
//        String id = idMapper.getId(pheno);
//        log.info("With id: {}", id);
//
//        MethodOutcome outcome = new MethodOutcome();
//        outcome.setId(new IdType().setValue(id));
//        return outcome;
//    }

    @Read
    @Transactional(readOnly = true)
    public IBaseResource read(@IdParam IdType idType) throws Exception {
        CompletableFuture<IBaseResource> cf = new CompletableFuture<>();
        this.idMapper.run(List.of(idType), id -> {
            try {
                PhenoKey key = (PhenoKey) id;
                log.info("Loading with id {}", id);

                Pheno pheno = repository.findById(key).orElseThrow(() -> {
                    log.error("{} with id {} not found", target, key);
                    return new ResourceNotFoundException(idType);
                });

                log.info("Loaded {}", pheno.getId());
                resourceMapping.run(List.of(pheno), fhir -> {
                    log.info("Converted: {}", fhir);
                    IBaseResource baseResource = (IBaseResource) fhir;
//                    String json = FhirContext.forR4B()
//                            .newJsonParser()
//                            .setPrettyPrint(true)
//                            .encodeResourceToString(baseResource);
//                    log.info("Converted: {}", json);
                    cf.complete(baseResource);
                });
            }catch (Exception e) {
                cf.completeExceptionally(e);
            }
        });

        return cf.get();
    }
}
