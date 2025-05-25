package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.core.Individual;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.OwnIdentifiers;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.IdManager;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.types.ListType;
import org.hl7.fhir.r4b.model.Identifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

@Component
public class IdMapper implements PhenoMapper<String, List<Identifier>> {
    private final Config config;
    private final IdManager idManager;

    public IdMapper(Config config, IdManager idManager) {
        this.config = config;
        this.idManager = idManager;
    }

    @Override
    public String toPheno(List<Identifier> identifier) throws Exception {
        OwnIdentifiers ownIdentifiers = config.getOwnIdentifiers();
        for(Identifier id : identifier) {
            if (ownIdentifiers.getSystem().equals(id.getSystem())) {
                return idManager.idAsCurie(id);
            }
        }
        Identifier id = idManager.getNewIdentifier();
        identifier.add(id);
        return idManager.idAsCurie(id);
    }

    @Override
    public Type getFhirClass() {
        return new ListType(Identifier.class);
    }

    @Override
    public Class<String> getPhenoClass() {
        return String.class;
    }
}
