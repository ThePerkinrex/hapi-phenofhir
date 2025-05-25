package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.key;

import entities.org.phenopackets.schema.v2.Phenopacket;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import org.hl7.fhir.r4b.model.IdType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class PhenopacketKeyIdTypeMapper implements PhenoMapper<Phenopacket.Key, IdType> {
    @Override
    public Phenopacket.Key toPheno(IdType idType) throws Exception {
        return new Phenopacket.Key(idType.getIdPart());
    }

    @Override
    public Type getFhirClass() {
        return IdType.class;
    }

    @Override
    public Type getPhenoClass() {
        return Phenopacket.Key.class;
    }
}
