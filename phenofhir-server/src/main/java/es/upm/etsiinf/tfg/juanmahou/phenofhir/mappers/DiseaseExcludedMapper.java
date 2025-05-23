package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperAlias;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperIgnore;
import org.hl7.fhir.r4b.model.CodeableConcept;
import org.hl7.fhir.r4b.model.Coding;
import org.springframework.stereotype.Component;

@MapperIgnore
@MapperAlias("DiseaseExcluded")
@Component
public class DiseaseExcludedMapper implements FhirMapper<Boolean, CodeableConcept>, PhenoMapper<Boolean, CodeableConcept> {

    @Override
    public Class<CodeableConcept> getFhirClass() {
        return CodeableConcept.class;
    }

    @Override
    public Class<Boolean> getPhenoClass() {
        return Boolean.class;
    }

    @Override
    public CodeableConcept toFHIR(Boolean b) throws Exception {
        if(b) {
            CodeableConcept c = new CodeableConcept();
            c.addCoding().setSystem("http://snomed.info/sct").setCode("315215002").setDisplay("Disease excluded");
            return c;
        }
        return null;
    }

    @Override
    public Boolean toPheno(CodeableConcept codeableConcept) throws Exception {
        if(codeableConcept != null) {
            for(Coding c : codeableConcept.getCoding()) {
                if ("http://snomed.info/sct".equals(c.getSystem()) && "315215002".equals(c.getCode())) {
                    return true;
                }
            }
        }
        return false;
    }
}
