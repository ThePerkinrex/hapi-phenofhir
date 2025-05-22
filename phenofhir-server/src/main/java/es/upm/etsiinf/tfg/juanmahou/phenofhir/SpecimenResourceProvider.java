package es.upm.etsiinf.tfg.juanmahou.phenofhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import entities.org.phenopackets.schema.v2.core.OntologyClass;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.BiosampleRepository;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.NotFoundException;
import jakarta.transaction.Transactional;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.*;
import entities.org.phenopackets.schema.v2.core.Biosample;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Component;

public class SpecimenResourceProvider implements IResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(SpecimenResourceProvider.class);

    private Mapper<OntologyClass, Coding> ontologyClassCodingMapper;
    private RepositoryProvider repositoryProvider;

    public SpecimenResourceProvider(MapperRegistry mapperRegistry, RepositoryProvider repositoryProvider) throws NotFoundException {
        ontologyClassCodingMapper = mapperRegistry.getMapper(OntologyClass.class, Coding.class);
        this.repositoryProvider = repositoryProvider;
    }

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Specimen.class;
    }

    /**
     * POST /fhir/Specimen
     * Accepts any Specimen, e.g. a biopsy. Returns the assigned ID.
     */
    @Create
    @Transactional
    public MethodOutcome create(@ResourceParam Specimen specimen) throws Exception {
        // Check for Biopsample:
        final String BIOSAMPLE_PROFILE = "http://hl7.org/fhir/uv/phenomics-exchange/StructureDefinition/Biosample";

        if (specimen.getMeta().getProfile().stream().anyMatch(p -> p.equals(BIOSAMPLE_PROFILE))) {
            log.info("Is biosample");
        }else{
            log.info("This is not a biosample");
            MethodOutcome outcome = new MethodOutcome();
            outcome.setCreated(false);
//        outcome.setId(new IdType("Specimen", saved.getIdElement().getIdPart()));
            return outcome;
        }

        log.info("Specimen: {}, profiles: {}", specimen, specimen.getMeta().getProfile());

        Biosample bs = new Biosample();
        bs.setId(new Biosample.Key().setId(specimen.getIdentifier().getFirst().getValue()));
        // Biosample Profile restricts this to be present
        // What
        Reference individual = specimen.getSubject();
        if(individual != null && !individual.isEmpty()) {
            bs.setIndividual_id(individual.getReference()); // TODO What
        }
        bs.setDescription(specimen.getNote().toString()); // How to convert to string correctly?
        if(specimen.hasCollection()) {
            var collection = specimen.getCollection();
            if (collection.hasBodySite()) {
                var bodySite = collection.getBodySite();
                if (!bodySite.getCoding().isEmpty()) {
                    var coding = bodySite.getCoding().getFirst();
                    bs.setSampled_tissue(ontologyClassCodingMapper.toPheno(coding));
                    CrudRepository<OntologyClass, OntologyClass.Key> ocr = repositoryProvider.getCrudRepository(OntologyClass.class);

                    ocr.save(bs.getSampled_tissue());
                }
            }
        }

        CrudRepository<Biosample, Biosample.Key> br = repositoryProvider.getCrudRepository(Biosample.class);

        br.save(bs);



        // Persist via your own layer
//        Specimen saved = specimenService.save(specimen);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setCreated(true);
        return outcome;
    }
}
