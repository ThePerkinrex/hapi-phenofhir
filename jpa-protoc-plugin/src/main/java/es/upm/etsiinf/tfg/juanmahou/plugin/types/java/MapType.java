package es.upm.etsiinf.tfg.juanmahou.plugin.types.java;

import es.upm.etsiinf.tfg.juanmahou.plugin.types.TypeRegistry;

import java.util.Objects;

public class MapType implements JavaType {

    private final JavaType key;
    private final JavaType value;

    public MapType(JavaType key, JavaType value) {
        this.key = key;
        this.value = value;
    }

    @Override
    public String toString() {
        return TypeRegistry.MAP.toString() + "<" + key.toString() + ", " + value.toString() + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        MapType mapType = (MapType) o;
        return Objects.equals(key, mapType.key) && Objects.equals(value, mapType.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(key, value);
    }

    public JavaType getKey() {
        return key;
    }

    public JavaType getValue() {
        return value;
    }

    @Override
    public boolean equals(JavaType other) {
        return this.equals((Object) other);
    }
}
