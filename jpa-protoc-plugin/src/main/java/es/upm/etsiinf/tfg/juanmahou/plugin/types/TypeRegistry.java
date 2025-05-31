package es.upm.etsiinf.tfg.juanmahou.plugin.types;

import com.google.protobuf.Descriptors.FieldDescriptor;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.ClassType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.JavaType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.PrimitiveType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.source.SourceType;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class TypeRegistry {
    private static final Map<SourceType, TypeMapping> TYPE_REGISTRY;

    static {
        TYPE_REGISTRY = new HashMap<>();
        addType(SourceType.build(FieldDescriptor.JavaType.BOOLEAN), new PrimitiveType("Boolean"));
        addType(SourceType.build(FieldDescriptor.JavaType.FLOAT), new PrimitiveType("Float"));
        addType(SourceType.build(FieldDescriptor.JavaType.DOUBLE), new PrimitiveType("Double"));
        addType(SourceType.build(FieldDescriptor.JavaType.INT), new PrimitiveType("Integer"));
        addType(SourceType.build(FieldDescriptor.JavaType.LONG), new PrimitiveType("Long"));
        addType(SourceType.build(FieldDescriptor.JavaType.STRING), new ClassType(String.class));

        addType(SourceType.build("google.protobuf.Timestamp"), new ClassType(Instant.class), f -> "com.google.protobuf.Timestamp.newBuilder().setSeconds(" + f + ".getEpochSecond()).setNanos(" + f + ".getNano()).build()");
    }

    private static void addType(SourceType source, JavaType javaType) {
        TYPE_REGISTRY.put(source, new TypeMapping(source, javaType));
    }
    private static void addType(SourceType source, JavaType javaType, Function<String, String> asProto) {
        TYPE_REGISTRY.put(source, new TypeMapping(source, javaType, asProto));
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
    public static final JavaType MANY_TO_MANY_ANNOTATION = new ClassType(JPA_BASE, "ManyToMany");
    public static final JavaType MANY_TO_ONE_ANNOTATION = new ClassType(JPA_BASE, "ManyToOne");
    public static final JavaType ONE_TO_MANY_ANNOTATION = new ClassType(JPA_BASE, "OneToMany");
    public static final JavaType ONE_TO_ONE_ANNOTATION = new ClassType(JPA_BASE, "OneToOne");
    public static final JavaType ELEMENT_COLLECTION_ANNOTATION = new ClassType(JPA_BASE, "ElementCollection");
    public static final JavaType COLUMN_ANNOTATION = new ClassType(JPA_BASE, "Column");
    public static final JavaType ENUMERATED_ANNOTATION = new ClassType(JPA_BASE, "Enumerated");
    public static final JavaType ENUM_TYPE = new ClassType(JPA_BASE, "EnumType");
    public static final JavaType LOB_ANNOTATION = new ClassType(JPA_BASE, "Lob");
    public static final JavaType MAP_KEY_COLUMN_ANNOTATION = new ClassType(JPA_BASE, "MapKeyColumn");
    public static final JavaType GENERATED_VALUE_ANNOTATION = new ClassType(JPA_BASE, "GeneratedValue");
    public static final JavaType FETCH_TYPE = new ClassType(JPA_BASE, "FetchType");
    public static final JavaType CASCADE_TYPE = new ClassType(JPA_BASE, "CascadeType");


    public static final JavaType LIST = new ClassType(List.class);
    public static final JavaType SET = new ClassType(Set.class);
    public static final JavaType MAP = new ClassType(Map.class);

    public static final JavaType ID_IFACE = new ClassType(Id.class);
    public static final JavaType WITH_ID_IFACE = new ClassType(WithId.class);
}
