package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.Phenopacket;
import entities.org.phenopackets.schema.v2.core.Individual;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.CurieManager;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.IdManager;
import org.hl7.fhir.r4.model.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class KeyMapper implements MapperClass {
    private static final Logger log = LoggerFactory.getLogger(KeyMapper.class);
    private final ObjectProvider<IdManager> idManager;
    private final ObjectProvider<CurieManager> curieManager;

    public KeyMapper(ObjectProvider<IdManager> idManager, ObjectProvider<CurieManager> curieManager) {
        this.idManager = idManager;
        this.curieManager = curieManager;
    }

    private Identifier getOwnIdentifier(List<Identifier> identifiers) {
        CurieManager.System own = curieManager.getObject().getOwnSystem();
        for(Identifier id : identifiers) {
            if(Objects.equals(own.system(), id.getSystem())) {
                return id;
            }
        }
        Identifier id = idManager.getObject().getNewIdentifier();
        try {
            identifiers.add(id);
        } catch (UnsupportedOperationException e) {
            log.error("Cant add id to identifier list", e);
        }
        return id;
    }

    @Mapper
    public Individual.Key mapIndividual(Context ctx, List<Identifier> identifiers) {
        Identifier id = getOwnIdentifier(identifiers);
        return new Individual.Key(idManager.getObject().idAsCurie(id));
    }

    @Mapper
    public List<String> mapIndividualAlternateIds(Context ctx, List<Identifier> identifiers) {
        IdManager idManager = this.idManager.getObject();
        return identifiers.stream().map(idManager::idAsCurie).toList();
    }

    @Mapper("PatientIds")
    public List<Identifier> mapPatientIdentifiers(Context ctx, List<String> alternateIds, Individual.Key id) {
        IdManager idManager = this.idManager.getObject(); // TODO support id
        return alternateIds.stream().map(idManager::curieAsId).toList();
    }


    @Mapper
    public Phenopacket.Key mapPhenopacket(Context<?> ctx, Identifier identifier) {
        return new Phenopacket.Key(idManager.getObject().idAsCurie(identifier));
    }

    @Mapper
    public Identifier mapCompositionIdentifier(Context<?> ctx, Phenopacket.Key identifier) {
        IdManager idManager = this.idManager.getObject();
        return idManager.curieAsId(identifier.getId());
    }
}
