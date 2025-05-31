package es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.simple;

import es.upm.etsiinf.tfg.juanmahou.plugin.render.protobuilder.BuilderCall;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;

import java.util.function.Function;

public class MapSimpleSet implements BuilderCall {
    private final String sourceFieldName;
    private final String setterName;
    private final Function<String, String> asProto;

    public MapSimpleSet(String fName, Function<String, String> asProto) {
        this.sourceFieldName = fName;
        this.setterName = "put" + CaseUtils.toUpperCamelCase(fName);
        this.asProto = asProto;
    }

    public MapSimpleSet(String fName) {
        this(fName, Function.identity());
    }

    @Override
    public String toString() {
        return "if(this." + sourceFieldName + " != null) for(var x : this." + sourceFieldName + ".entrySet()) { builder." + setterName + "(x.getKey()," + asProto.apply("x.getValue()") + "); }";
    }
}
