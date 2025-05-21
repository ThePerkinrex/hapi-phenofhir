package es.upm.etsiinf.tfg.juanmahou.plugin.types.java;

import java.util.Objects;

public class PrimitiveType implements JavaType {
    private final String name;

    public PrimitiveType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PrimitiveType that = (PrimitiveType) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(JavaType other) {
        return this.equals((Object) other);
    }
}
