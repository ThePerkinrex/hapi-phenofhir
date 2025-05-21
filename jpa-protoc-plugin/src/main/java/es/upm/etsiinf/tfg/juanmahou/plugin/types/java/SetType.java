package es.upm.etsiinf.tfg.juanmahou.plugin.types.java;

import es.upm.etsiinf.tfg.juanmahou.plugin.types.TypeRegistry;

import java.util.Objects;

public class SetType implements JavaType {

    private final JavaType type;

    public SetType(JavaType type) {
        this.type = type;
    }

    public JavaType getType() {
        return type;
    }

    @Override
    public String toString() {
        return TypeRegistry.SET.toString() + "<" + type.toString() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        SetType listType = (SetType) o;
        return Objects.equals(type, listType.type);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public boolean equals(JavaType other) {
        return this.equals((Object) other);
    }
}
