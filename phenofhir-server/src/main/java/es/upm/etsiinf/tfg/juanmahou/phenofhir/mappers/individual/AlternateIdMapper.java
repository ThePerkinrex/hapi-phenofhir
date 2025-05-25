package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.individual;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.IdManager;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.types.ListType;
import org.hl7.fhir.r4b.model.Identifier;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

@Component
public class AlternateIdMapper implements Mapper<List<String>, List<Identifier>> {
    public AlternateIdMapper(IdManager idManager) {
        this.idManager = idManager;
    }

    private final IdManager idManager;

    @Override
    public List<String> toPheno(List<Identifier> identifiers) throws Exception {
        List<String> curies = new ArrayList<>(identifiers.size());
        for (Identifier id : identifiers) {
            curies.add(idManager.idAsCurie(id));
        }
        return curies;
    }

    @Override
    public Type getFhirClass() {
        return new ListType(Identifier.class);
    }

    @Override
    public Type getPhenoClass() {
        return new ListType(String.class);
    }

    @Override
    public List<Identifier> toFHIR(List<String> identifiers) throws Exception {
        List<Identifier> curies = new ArrayList<>(identifiers.size());
        for (String id : identifiers) {
            curies.add(idManager.curieAsId(id));
        }
        return curies;
    }
}
