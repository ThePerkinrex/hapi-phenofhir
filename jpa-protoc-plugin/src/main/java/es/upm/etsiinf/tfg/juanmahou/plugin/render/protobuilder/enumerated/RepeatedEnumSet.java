package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.enumerated;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

public class RepeatedEnumSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;

    public RepeatedEnumSet(String fName) {
        this.sourceFieldName = fName;
        this.setterName = "add" + CaseUtils.toUpperCamelCase(fName) + "Value";
    }

    @Override
    public String toString() {
        return "if(this." + sourceFieldName + " != null) for(var x : this." + sourceFieldName + ") { builder." + setterName + "(x.getNumber()); }";
    }
}
