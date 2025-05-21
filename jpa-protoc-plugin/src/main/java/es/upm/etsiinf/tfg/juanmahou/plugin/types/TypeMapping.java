package es.upm.etsiinf.tfg.juanmahou.plugin.types;

import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.JavaType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.ListType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.SetType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.source.RepeatedSourceType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.source.SourceType;

import java.util.Objects;

public class TypeMapping {
    private final SourceType source;
    private final JavaType javaType;

    public TypeMapping(SourceType source, JavaType javaType) {
        this.source = source;
        this.javaType = javaType;
    }

    public SourceType getSource() {
        return source;
    }

    public JavaType getJavaType() {
        return javaType;
    }

    public TypeMapping toRepeated() {
        return new TypeMapping(new RepeatedSourceType(source), new ListType(javaType));
    }

    public TypeMapping toSet() {
        return new TypeMapping(new RepeatedSourceType(source), new SetType(javaType));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        TypeMapping that = (TypeMapping) o;
        return Objects.equals(source, that.source) && Objects.equals(javaType, that.javaType);
    }

    @Override
    public int hashCode() {
        return Objects.hash(source, javaType);
    }

    @Override
    public String toString() {
        return "TypeMapping{" +
                "source=" + source +
                ", javaType=" + javaType +
                '}';
    }
}
