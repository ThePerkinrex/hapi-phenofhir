package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.pk;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

public class RepeatedPkSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;

    public RepeatedPkSet(String fName) {
        this.sourceFieldName = fName;
        this.setterName = "add" + CaseUtils.toUpperCamelCase(fName);
    }

    @Override
    public String toString() {
        return "if(this." + sourceFieldName + " != null) for(var x : this." + sourceFieldName + ") { builder.id." + setterName + "(x); }";
    }
}
