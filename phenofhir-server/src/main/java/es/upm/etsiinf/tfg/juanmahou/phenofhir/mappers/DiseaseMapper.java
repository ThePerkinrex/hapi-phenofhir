package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.core.OntologyClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.hl7.fhir.r4b.model.CodeableConcept;
import org.hl7.fhir.r4b.model.Condition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DiseaseMapper implements MapperClass {
    private static final Logger log = LoggerFactory.getLogger(DiseaseMapper.class);

    @Mapper("diseaseStage")
    public Set<OntologyClass> diseaseStageMapper(Context<?> ctx, List<Condition.ConditionStageComponent> stages) {
        log.warn("TODO diseaseStage"); // TODO
        return new HashSet<>();
    }

    @Mapper("primarySite")
    public OntologyClass primarySiteMapper(Context<?> ctx, List<CodeableConcept> stages) {
        log.warn("TODO primarySite"); // TODO
        return null;
    }
}
