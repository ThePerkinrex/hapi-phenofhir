package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRunner;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.Mapping;
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

import javax.naming.OperationNotSupportedException;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GeneralPhenomicResource<PhenoKey extends Id, Pheno extends WithId<PhenoKey>> implements IResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(GeneralPhenomicResource.class);
    private final Class<? extends IBaseResource> resource;
    private final ResolvableType target;
    private final Mapping mapping;
    private final MapperRunner resourceMapping;
//    private final PhenoMapper<PhenoKey, IdType> idMapper;
    private final CrudRepository<Pheno, PhenoKey> repository;


    public GeneralPhenomicResource(
            ResolvableType target,
            ResolvableType resource,
            Mapping mapping,
            MapperRunner mapper,
            RepositoryProvider repositoryProvider,
            MapperRegistry registry
    ) {
        if(!ResolvableType.forClass(IBaseResource.class).isAssignableFrom(resource)) throw new RuntimeException("Cant create resource provider for " + resource);
        this.resource = (Class<? extends IBaseResource>) resource.resolve();
        this.mapping = mapping;
        this.target = target;
        this.resourceMapping = mapper;
//        this.idMapper = (PhenoMapper<PhenoKey, IdType>) registry.getKeyMapper(target);
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
    public Object read(@IdParam IdType idType) throws Exception {
//        PhenoKey key = this.idMapper.toPheno(idType);
//        Pheno pheno = repository.findById(key).orElseThrow(() -> {
//            log.error("{} with id {} not found", target, key);
//            return new ResourceNotFoundException(idType);
//        });

//        PhenoKey id;
//        try {
//            id = idMapper.getId(target, idStr);
//        } catch (ClassNotFoundException | JsonProcessingException e) {
//            log.error("Error reading id", e);
//            throw new ResourceNotFoundException("Invalid id");
//        }
//
//
//        Pheno pheno = repository.findById(id).orElseThrow(() -> {
//            log.error("{} with id {} not found", target, id);
//            return new ResourceNotFoundException(idType);
//        });
//        log.info("Loaded {}", pheno.getId());
//        FHIR fhir = resourceMapping.toFHIR(pheno);
//        log.info("Converted: {}", fhir);
//
//        return fhir;
        throw new OperationNotSupportedException("Not implemented");
    }
}
