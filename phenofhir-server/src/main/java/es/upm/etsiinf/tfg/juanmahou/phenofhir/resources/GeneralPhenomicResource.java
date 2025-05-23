package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException;
import com.fasterxml.jackson.core.JsonProcessingException;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.id.IdMapper;
import jakarta.transaction.Transactional;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.CodeableConcept;
import org.hl7.fhir.r4b.model.IdType;
import org.hl7.fhir.r4b.model.OperationOutcome;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GeneralPhenomicResource<PhenoKey extends Id, Pheno extends WithId<PhenoKey>, FHIR extends IBaseResource> implements IResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(GeneralPhenomicResource.class);
    private final Class<FHIR> resource;
    private final Class<Pheno> target;
    private final Mapping mapping;
    private final Mapper<Pheno, FHIR> resourceMapping;
    private final IdMapper idMapper;
    private final CrudRepository<Pheno, PhenoKey> repository;


    public GeneralPhenomicResource(
            Class<Pheno> target,
            Class<FHIR> resource,
            Mapping mapping,
            Mapper<Pheno,FHIR> mapper,
            IdMapper idMapper,
            RepositoryProvider repositoryProvider
    ) {
        this.resource = resource;
        this.mapping = mapping;
        this.target = target;
        this.resourceMapping = mapper;
        this.idMapper = idMapper;
        this.repository = repositoryProvider.getCrudRepository(target);
    }

    @Override
    public Class<FHIR> getResourceType() {
        return resource;
    }

    @Create
    @Transactional
    public MethodOutcome create(@ResourceParam FHIR resource) throws Exception {
        if (resource.getMeta().getProfile().stream().noneMatch(p -> p.getValueAsString().equals(mapping.getProfile()))) {
            MethodOutcome outcome = new MethodOutcome();
            outcome.setCreated(false);
            var oo = new OperationOutcome();
            oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setCode(OperationOutcome.IssueType.STRUCTURE).setDetails(new CodeableConcept().setText("Resource didn't include the correct profile"));
            outcome.setOperationOutcome(oo);
            return outcome;
        }

        WithId<? extends Id> pheno = resourceMapping.toPheno(resource);
        log.info("Created {}", pheno);
        String id = idMapper.getId(pheno);
        log.info("With id: {}", id);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setId(new IdType().setValue(id));
        return outcome;
    }

    @Read
    public FHIR read(@IdParam IdType idType) throws Exception {
        String idStr = idType.getIdPart();
        PhenoKey id;
        try {
            id = idMapper.getId(target, idStr);
        } catch (ClassNotFoundException | JsonProcessingException e) {
            log.error("Error reading id", e);
            throw new ResourceNotFoundException("Invalid id");
        }


        Pheno pheno = repository.findById(id).orElseThrow(() -> {
            log.error("{} with id {} not found", target, id);
            return new ResourceNotFoundException(idType);
        });
        FHIR fhir = resourceMapping.toFHIR(pheno);
        log.info("Converted: {}", fhir);

        return fhir;
    }
}
