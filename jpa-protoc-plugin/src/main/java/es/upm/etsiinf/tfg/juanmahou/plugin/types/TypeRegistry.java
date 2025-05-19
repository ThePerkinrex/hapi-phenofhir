package es.upm.etsiinf.tfg.juanmahou.plugin.types;

import com.google.protobuf.Descriptors.FieldDescriptor;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.source.SourceType;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TypeRegistry {
    private static final Map<SourceType, TypeMapping> TYPE_REGISTRY;

    static {
        TYPE_REGISTRY = new HashMap<>();
        addType(SourceType.build(FieldDescriptor.JavaType.BOOLEAN), new PrimitiveType("boolean"));
        addType(SourceType.build(FieldDescriptor.JavaType.FLOAT), new PrimitiveType("float"));
        addType(SourceType.build(FieldDescriptor.JavaType.DOUBLE), new PrimitiveType("double"));
        addType(SourceType.build(FieldDescriptor.JavaType.INT), new PrimitiveType("int"));
        addType(SourceType.build(FieldDescriptor.JavaType.LONG), new PrimitiveType("long"));
        addType(SourceType.build(FieldDescriptor.JavaType.STRING), new PrimitiveType("String"));

        addType(SourceType.build("google.protobuf.Timestamp"), new ClassType(Instant.class));
    }

    private static void addType(SourceType source, JavaType javaType) {
        TYPE_REGISTRY.put(source, new TypeMapping(source, javaType));
    }

    public static TypeMapping getMapping(SourceType source) {
        return TYPE_REGISTRY.get(source);
    }

    public static TypeMapping getPrimitive(FieldDescriptor.JavaType source) {
        return TYPE_REGISTRY.get(SourceType.build(source));
    }

    public static TypeMapping getMessage(String source) {
        return TYPE_REGISTRY.get(SourceType.build(source));
    }

    private static final String JPA_BASE = "jakarta.persistence";
    public static final JavaType ENTITY_ANNOTATION = new ClassType(JPA_BASE, "Entity");
    public static final JavaType EMBEDDABLE_ANNOTATION = new ClassType(JPA_BASE, "Embeddable");
    public static final JavaType EMBEDDED_ANNOTATION = new ClassType(JPA_BASE, "Embedded");
    public static final JavaType EMBEDDED_ID_ANNOTATION = new ClassType(JPA_BASE, "EmbeddedId");
    public static final JavaType JOIN_TABLE_ANNOTATION = new ClassType(JPA_BASE, "JoinTable");
    public static final JavaType JOIN_COLUMN_ANNOTATION = new ClassType(JPA_BASE, "JoinColumn");
    public static final JavaType JOIN_COLUMNS_ANNOTATION = new ClassType(JPA_BASE, "JoinColumns");
    public static final JavaType ONE_TO_MANY_ANNOTATION = new ClassType(JPA_BASE, "OneToMany");
    public static final JavaType ONE_TO_ONE_ANNOTATION = new ClassType(JPA_BASE, "OneToOne");


}
