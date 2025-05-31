package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.enumerated;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

public class EnumSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;

    public EnumSet(String fName) {
        sourceFieldName = fName;
        setterName = "set" + CaseUtils.toUpperCamelCase(fName) + "Value";
    }

    @Override
    public String toString() {
        return "builder." + setterName + "(this." + sourceFieldName + ".getNumber());";
    }
}
