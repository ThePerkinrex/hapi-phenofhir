package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import entities.org.phenopackets.schema.v2.core.Biosample;
import entities.org.phenopackets.schema.v2.core.OntologyClass;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import jakarta.transaction.Transactional;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.CodeableConcept;
import org.hl7.fhir.r4b.model.OperationOutcome;
import org.hl7.fhir.r4b.model.Reference;
import org.hl7.fhir.r4b.model.Specimen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class GeneralPhenomicResource implements IResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(GeneralPhenomicResource.class);
    private final Class<? extends IBaseResource> resource;
    private final Class<?> target;
    private final Mapping mapping;
    private final Mapper<?, IBaseResource> resourceMapping;

    public GeneralPhenomicResource(Class<?> target, Class<? extends IBaseResource> resource, Mapping mapping, Mapper<?, IBaseResource> mapper) throws ClassNotFoundException, NoSuchMethodException {
        this.resource = resource;
        this.mapping = mapping;
        this.target = target;
        this.resourceMapping = mapper;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return resource;
    }

    @Create
    @Transactional
    public MethodOutcome create(@ResourceParam IBaseResource resource) throws Exception {
        if(resource.getMeta().getProfile().stream().noneMatch(p -> p.getValueAsString().equals(mapping.getProfile()))) {
            MethodOutcome outcome = new MethodOutcome();
            outcome.setCreated(false);
            var oo = new OperationOutcome();
            oo.addIssue().setSeverity(OperationOutcome.IssueSeverity.ERROR).setCode(OperationOutcome.IssueType.STRUCTURE).setDetails(new CodeableConcept().setText("Resource didn't include the correct profile"));
            outcome.setOperationOutcome(oo);
            return outcome;
        }

        Object pheno = resourceMapping.toPheno(resource);
        log.info("Created {}", pheno);

        MethodOutcome outcome = new MethodOutcome();
        return outcome;
    }
}
