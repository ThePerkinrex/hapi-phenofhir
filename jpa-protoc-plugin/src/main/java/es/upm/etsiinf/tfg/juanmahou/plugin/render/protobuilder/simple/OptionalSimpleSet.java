package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.simple;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

import java.util.function.Function;

public class OptionalSimpleSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;
    private final String clearerName;
    private final Function<String, String> asProto;

    public OptionalSimpleSet(String fName, Function<String, String> asProto) {
        this.sourceFieldName = fName;
        this.setterName = "set" + CaseUtils.toUpperCamelCase(fName);
        this.clearerName = "clear" + CaseUtils.toUpperCamelCase(fName);
        this.asProto = asProto;
    }

    public OptionalSimpleSet(String fName) {
        this(fName, Function.identity());
    }

    @Override
    public String toString() {
        return "if (this." + sourceFieldName + " == null) { builder." + clearerName + "(); } else{ builder." + setterName + "(" + asProto.apply("this." + sourceFieldName) + ");}";
    }
}
