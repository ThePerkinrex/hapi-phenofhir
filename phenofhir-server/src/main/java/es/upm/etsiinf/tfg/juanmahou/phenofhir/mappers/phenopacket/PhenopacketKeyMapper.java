package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.phenopacket;

import entities.org.phenopackets.schema.v2.Phenopacket;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.IdManager;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import org.hl7.fhir.r4b.model.Identifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class PhenopacketKeyMapper implements Mapper<Phenopacket.Key, Identifier> {
    private final IdManager idManager;

    public PhenopacketKeyMapper(IdManager idManager) {
        this.idManager = idManager;
    }

    @Override
    public Identifier toFHIR(Phenopacket.Key key) throws Exception {
        return idManager.curieAsId(key.getId());
    }

    @Override
    public Phenopacket.Key toPheno(Identifier identifier) throws Exception {
        return new Phenopacket.Key(idManager.idAsCurie(identifier));
    }

    @Override
    public Type getFhirClass() {
        return Identifier.class;
    }

    @Override
    public Type getPhenoClass() {
        return Phenopacket.Key.class;
    }
}
