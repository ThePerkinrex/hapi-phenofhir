package es.upm.etsiinf.tfg.juanmahou.phenofhir;

import ca.uhn.fhir.rest.annotation.Create;
import ca.uhn.fhir.rest.annotation.ResourceParam;
import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.Specimen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SpecimenResourceProvider implements IResourceProvider {
    private static final Logger log = LoggerFactory.getLogger(SpecimenResourceProvider.class);

    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Specimen.class;
    }

    /**
     * POST /fhir/Specimen
     * Accepts any Specimen, e.g. a biopsy. Returns the assigned ID.
     */
    @Create
    public MethodOutcome create(@ResourceParam Specimen specimen) {
        // Optional: enforce that specimen.type is a Biopsy code

        log.info("Specimen: {}, profiles: {}", specimen, specimen.getMeta().getProfile());

        // Persist via your own layer
//        Specimen saved = specimenService.save(specimen);

        MethodOutcome outcome = new MethodOutcome();
        outcome.setCreated(true);
//        outcome.setId(new IdType("Specimen", saved.getIdElement().getIdPart()));
        return outcome;
    }
}
