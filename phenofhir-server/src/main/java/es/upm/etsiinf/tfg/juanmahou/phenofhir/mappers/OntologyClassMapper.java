package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.core.OntologyClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.CurieManager;
import org.hl7.fhir.r4.model.CodeableConcept;
import org.hl7.fhir.r4.model.Coding;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
public class OntologyClassMapper implements MapperClass {
    private final ObjectProvider<CurieManager> curieManager;

    public OntologyClassMapper(ObjectProvider<CurieManager> curieManager) {
        this.curieManager = curieManager;
    }

    @Mapper
    public OntologyClass fromCodeableConcept(Context<?> ctx, CodeableConcept codeableConcept) {
        if (codeableConcept.getCoding().isEmpty()) throw new RuntimeException("A least a coding expected");
        Coding coding = codeableConcept.getCodingFirstRep();
        return new OntologyClass()
                .setId(
                        new OntologyClass.Key(
                                curieManager
                                        .getObject()
                                        .getCurieForSystem(new CurieManager.System(coding.getSystem(), coding.getSystem())
                                        ) + ":" + coding.getCode()
                        )
                ).setLabel(coding.getDisplay());
    }


    @Mapper("excluded")
    public boolean isExcluded(Context<?> ctx, CodeableConcept codeableConcept) {
        CurieManager curieManager = this.curieManager.getObject();
        CurieManager.System system = curieManager.getSystemForCurie("snomed");
        boolean excluded = false;
        for(Coding coding : codeableConcept.getCoding()) {
            if(system.system().equals(coding.getSystem()) && "315215002".equals(coding.getCode())) {
                return true;
            }
        }
        return excluded;
    }
}
