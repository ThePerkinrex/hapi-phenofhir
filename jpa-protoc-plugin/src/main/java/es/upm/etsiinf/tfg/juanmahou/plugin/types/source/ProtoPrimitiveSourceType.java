package es.upm.etsiinf.tfg.juanmahou.plugin.types.source;

import com.google.protobuf.Descriptors;

import java.util.Objects;

public class ProtoPrimitiveSourceType implements SourceType {
    private final Descriptors.FieldDescriptor.JavaType type;

    public ProtoPrimitiveSourceType(Descriptors.FieldDescriptor.JavaType type) {
        this.type = type;
    }

    public Descriptors.FieldDescriptor.JavaType getType() {
        return type;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProtoPrimitiveSourceType that = (ProtoPrimitiveSourceType) o;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(type);
    }

    @Override
    public String toString() {
        return "ProtoPrimitiveSourceType{" +
                "type=" + type +
                '}';
    }
}
