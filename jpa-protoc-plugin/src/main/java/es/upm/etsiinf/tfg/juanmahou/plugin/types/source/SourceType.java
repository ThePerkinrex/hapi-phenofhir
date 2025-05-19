package es.upm.etsiinf.tfg.juanmahou.plugin.types.source;

import com.google.protobuf.Descriptors;

public interface SourceType {
    static SourceType build(Descriptors.FieldDescriptor.JavaType type) {
        return new ProtoPrimitiveSourceType(type);
    }

    static SourceType build(String message) {
        return new ProtoMessageSourceType(message);
    }
}
