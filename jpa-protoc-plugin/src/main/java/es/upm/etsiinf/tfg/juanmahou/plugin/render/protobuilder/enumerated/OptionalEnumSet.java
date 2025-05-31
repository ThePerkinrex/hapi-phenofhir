package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.enumerated;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

public class OptionalEnumSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;
    private final String clearerName;

    public OptionalEnumSet(String fName) {
        this.sourceFieldName = fName;
        this.setterName = "set" + CaseUtils.toUpperCamelCase(fName) + "Value";
        this.clearerName = "clear" + CaseUtils.toUpperCamelCase(fName);
    }

    @Override
    public String toString() {
        return "if (this." + sourceFieldName + " == null) { builder." + clearerName + "(); } else{ builder." + setterName + "(this." + sourceFieldName + ".getNumber());}";
    }
}
