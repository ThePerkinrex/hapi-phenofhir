package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.nested;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

public class OptionalNestedSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;
    private final String clearerName;

    public OptionalNestedSet(String fName) {
        this.sourceFieldName = fName;
        this.setterName = "set" + CaseUtils.toUpperCamelCase(fName);
        this.clearerName = "clear" + CaseUtils.toUpperCamelCase(fName);
    }

    @Override
    public String toString() {
        return "if (this." + sourceFieldName + " == null) { builder." + clearerName + "(); } else{ builder." + setterName + "(this." + sourceFieldName + ".asPheno());}";
    }
}
