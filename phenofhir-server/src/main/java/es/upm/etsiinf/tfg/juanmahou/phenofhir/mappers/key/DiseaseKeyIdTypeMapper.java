package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.key;

import entities.org.phenopackets.schema.v2.core.Disease;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import org.hl7.fhir.r4b.model.IdType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class DiseaseKeyIdTypeMapper implements PhenoMapper<Disease.Key, IdType> {
    @Override
    public Disease.Key toPheno(IdType idType) throws Exception {
        return new Disease.Key(Long.parseLong(idType.getIdPart()));
    }

    @Override
    public Type getFhirClass() {
        return IdType.class;
    }

    @Override
    public Type getPhenoClass() {
        return Disease.Key.class;
    }
}
