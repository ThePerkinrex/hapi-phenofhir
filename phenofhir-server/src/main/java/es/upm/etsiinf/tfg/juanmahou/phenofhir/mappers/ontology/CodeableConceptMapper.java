package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.ontology;

import entities.org.phenopackets.schema.v2.core.OntologyClass;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import org.hl7.fhir.r4b.model.CodeableConcept;
import org.springframework.stereotype.Component;

@Component
public class CodeableConceptMapper implements Mapper<OntologyClass, CodeableConcept> {
    private final CodingMapper mapper;

    public CodeableConceptMapper(CodingMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public Class<CodeableConcept> getFhirClass() {
        return CodeableConcept.class;
    }

    @Override
    public Class<OntologyClass> getPhenoClass() {
        return OntologyClass.class;
    }

    @Override
    public CodeableConcept toFHIR(OntologyClass ontologyClass) throws Exception {
        return new CodeableConcept().addCoding(mapper.toFHIR(ontologyClass));
    }

    @Override
    public OntologyClass toPheno(CodeableConcept codeableConcept) throws Exception {
        return mapper.toPheno(codeableConcept.getCodingFirstRep());
    }
}
