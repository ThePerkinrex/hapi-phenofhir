package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.nested;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

public class NestedSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;

    public NestedSet(String fName) {
        sourceFieldName = fName;
        setterName = "set" + CaseUtils.toUpperCamelCase(fName);
    }

    @Override
    public String toString() {
        return "builder." + setterName + "(this." + sourceFieldName + ".asPheno());";
    }
}
