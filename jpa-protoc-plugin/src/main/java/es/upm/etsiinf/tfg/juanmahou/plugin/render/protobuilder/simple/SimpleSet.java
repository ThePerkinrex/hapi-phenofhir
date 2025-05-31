package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.simple;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

import java.util.function.Function;

public class SimpleSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;
    private final Function<String, String> asProto;

    public SimpleSet(String sourceFieldName, String setterName, Function<String, String> asProto) {
        this.sourceFieldName = sourceFieldName;
        this.setterName = setterName;
        this.asProto = asProto;
    }

    public SimpleSet(String fName, Function<String, String> asProto) {
        this(fName, "set" + CaseUtils.toUpperCamelCase(fName), asProto);
    }

    public SimpleSet(String fName) {
        this(fName, Function.identity());
    }

    @Override
    public String toString() {
        return "builder." + setterName + "(" + asProto.apply("this." + sourceFieldName) + ");";
    }
}
