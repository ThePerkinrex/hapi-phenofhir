package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.individual;

import entities.org.phenopackets.schema.v2.core.Individual;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.IdMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import org.hl7.fhir.r4b.model.Identifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

@Component
public class IndividualKeyMapper implements PhenoMapper<Individual.Key, List<Identifier>> {
    private final IdMapper idMapper;

    public IndividualKeyMapper(IdMapper idMapper) {
        this.idMapper = idMapper;
    }

    @Override
    public Individual.Key toPheno(List<Identifier> identifier) throws Exception {
        return new Individual.Key(idMapper.toPheno(identifier));
    }

    @Override
    public Type getFhirClass() {
        return idMapper.getFhirClass();
    }

    @Override
    public Class<Individual.Key> getPhenoClass() {
        return Individual.Key.class;
    }
}
