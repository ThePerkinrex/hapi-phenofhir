package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.key;

import entities.org.phenopackets.schema.v2.core.Individual;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import org.hl7.fhir.r4b.model.IdType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class IndividualKeyIdTypeMapper implements PhenoMapper<Individual.Key, IdType> {
    @Override
    public Individual.Key toPheno(IdType idType) throws Exception {
        return new Individual.Key(idType.getIdPart());
    }

    @Override
    public Type getFhirClass() {
        return IdType.class;
    }

    @Override
    public Type getPhenoClass() {
        return Individual.Key.class;
    }
}
