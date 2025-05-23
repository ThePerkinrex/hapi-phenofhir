package es.upm.etsiinf.tfg.juanmahou.plugin.tables;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import es.upm.etsiinf.tfg.juanmahou.plugin.DependencyManager;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Cardinality;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.ConfigCardinality;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.ConfigField;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.ConfigTable;
import es.upm.etsiinf.tfg.juanmahou.plugin.render.Accessor;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.field.*;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.providers.FilePackageProvider;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.providers.PackageProvider;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.Annotation;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.TypeMapping;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.TypeRegistry;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.*;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.source.ProtoMessageSourceType;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.source.SourceType;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.HashUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Concrete implementation of a Table, managing fields, keys, and generation logic.
 */
public class Table {
    public static final String LAZY_FETCH = "fetch = " + TypeRegistry.FETCH_TYPE + ".LAZY";
    public static final String OPTIONAL_TRUE = "optional = true";
    public static final String OPTIONAL_FALSE = "optional = false";
    public static final String NULLABLE_TRUE = "nullable = true";
    public static final String NULLABLE_FALSE = "nullable = false";

    public static final String CASCADE_ALL = "cascade = " + TypeRegistry.CASCADE_TYPE + ".ALL";


    private static final Logger logger = LoggerFactory.getLogger(Table.class);

    private final String createdBy;
    private final TableManager manager;
    private final ConfigTable config;
    private final String name;
    private final PackageProvider packageProvider;
    private final Predicate<String> isNestedType;

    private final Set<String> primaryKey = new LinkedHashSet<>();
    private final Map<String, Field> fields = new LinkedHashMap<>();
//    private final List<ForeignKey> foreignKeys = new ArrayList<>();
    private final Map<String, OneOf> oneOfs = new LinkedHashMap<>();
    private final Map<JavaType, List<Map.Entry<String, Integer>>> enums = new LinkedHashMap<>();
//    private final List<Table> joinTables = new ArrayList<>();
    private final Map<String, AbstractFieldDescriptor> fieldDescriptors;

    public Table(TableManager manager,
                 ConfigTable config,
                 String name,
                 PackageProvider packageProvider,
                 Map<String, AbstractFieldDescriptor> fieldDescriptors,
                 String createdBy,
                 Predicate<String> isNestedType) {
        this.manager = Objects.requireNonNull(manager);
        this.config = Objects.requireNonNull(config);
        this.name = Objects.requireNonNull(name);
        this.packageProvider = Objects.requireNonNull(packageProvider);
        this.fieldDescriptors = new LinkedHashMap<>(Objects.requireNonNull(fieldDescriptors));
        this.createdBy = Objects.requireNonNull(createdBy);
        this.isNestedType = Objects.requireNonNull(isNestedType);
        initialize();
    }

    private void initialize() {
        logger.info("> Starting {} as {} by {}", fullName(), name, createdBy);
        DependencyManager.getInstance().register(fullName());

        // determine primary key
        List<String> pk = config.getPrimaryKey();
        if (!pk.isEmpty()) {
            pk.forEach(col -> {
                if (!fieldDescriptors.containsKey(col)) {
                    throw new IllegalStateException(
                            String.format("PK field '%s' missing for %s", col, fullName()));
                }
            });
            primaryKey.addAll(pk);
        } else if (config.isInsert()) {
            String synthetic = "id";
            if (fieldDescriptors.containsKey(synthetic)) {
                synthetic += "_" + HashUtils.hashSuffix(fullName());
                logger.warn("-- 'id' collision; using synthetic PK '{}' for {}", synthetic, fullName());
            } else {
                logger.info("-- Generating synthetic PK '{}' for {}", synthetic, fullName());
            }
            primaryKey.add(synthetic);
            JavaType syntheticType = new PrimitiveType("Long");
            List<Annotation> annotations = List.of(new Annotation(TypeRegistry.GENERATED_VALUE_ANNOTATION));
            // TODO Handle custom generators
            Field f = new Field(this, synthetic, syntheticType, annotations);
            addField(f);
            // TODO: add descriptor for synthetic if desired
        } else {
            throw new IllegalStateException("No primary key for " + fullName());
        }

        // process each field
        fieldDescriptors.values().forEach(this::processField);

        logger.info("> Finished {} as {}", fullName(), name);
    }

    private void processField(AbstractFieldDescriptor fd) {
        OneOf oneOfCont = null;

        // handle oneof
        String oneofName = fd.getOneof();
        if (oneofName != null) {
            logger.info("field {} is part of oneof {}", fd.getName(), oneofName);
            ConfigField oc = config.getFields().getOrDefault(oneofName, new ConfigField());
            Cardinality card = oc.getCardinality()
                    .resolve(() -> Cardinality.OPTIONAL);
            if (card == Cardinality.REPEATED) {
                logger.error("Repeated cardinality in oneof {} not supported", oneofName);
                return;
            }
            oneOfCont = addOneOf(oneofName, oc.getCardinality() == ConfigCardinality.REQUIRED);
        }


        // Map fields
        if (fd instanceof MapFieldDescriptor mfd) {
            Descriptors.Descriptor msg = mfd.getMessageType();
            FieldDescriptor keyPb = msg.findFieldByName("key");
            FieldDescriptor valPb = msg.findFieldByName("value");

            ConfigField keyCfg = new ConfigField();
            keyCfg.setCardinality(ConfigCardinality.REQUIRED);
            ConfigField valCfg = new ConfigField();
            valCfg.setCardinality(ConfigCardinality.REQUIRED);
            AbstractFieldDescriptor keyDesc = AbstractFieldDescriptor.build(keyPb, keyCfg);
            AbstractFieldDescriptor valDesc = AbstractFieldDescriptor.build(valPb, valCfg);


            if (keyDesc instanceof PrimitiveFieldDescriptor pKeyDesc) {
                TypeMapping key = TypeRegistry.getPrimitive(pKeyDesc.getType());
                JavaType val;
                List<Annotation> annotations = new ArrayList<>(List.of(new Annotation(TypeRegistry.ELEMENT_COLLECTION_ANNOTATION), new Annotation(TypeRegistry.MAP_KEY_COLUMN_ANNOTATION)));
                if (valDesc instanceof PrimitiveFieldDescriptor pValDesc) {
                    val = TypeRegistry.getPrimitive(pValDesc.getType()).getJavaType();

                } else if (valDesc instanceof MessageFieldDescriptor mValDesc) {
                    Descriptors.Descriptor messageType = mValDesc.getMessageType();

                    TypeMapping mapping = TypeRegistry.getMessage(messageType.getFullName());
                    if (mapping != null) {
                        val = mapping.getJavaType();
                    }else{
                        String nestedId = messageType.getFullName();
                        ConfigTable cfg = manager.getConfig(nestedId);
                        if (cfg != null  && cfg.isAsProtobuf()) {
                            val = new PrimitiveType("byte[]");
                            annotations.add(new Annotation(TypeRegistry.LOB_ANNOTATION));
                        }else{
                            String src = "nested#" + fullName();
                            Table child = manager.getTable(nestedId, src);
                            if(child == null) {
                                logger.error("{} not configured", nestedId);
                                return;
                            }

                            val = child.getJavaType();
                            annotations = List.of(new Annotation(TypeRegistry.MANY_TO_MANY_ANNOTATION), new Annotation(TypeRegistry.MAP_KEY_COLUMN_ANNOTATION));

                        }
                    }
                }else{
                    logger.error("Map value is not a primitive type or a message type");
                    return;
                }
                JavaType map = new MapType(key.getJavaType(), val);
                Field f = new Field(this, mfd.getName(), map, annotations);
                addField(f, oneOfCont);
                return;
            }else{
                logger.error("Map key is not a primitive type");
                return;
            }
        }

        // Message fields
        if (fd instanceof MessageFieldDescriptor mfd) {
            Descriptors.Descriptor messageType = mfd.getMessageType();
            TypeMapping mapping = TypeRegistry.getMessage(messageType.getFullName());
            if (mapping != null) {
                List<Annotation> annotations = new ArrayList<>(1);
                switch (mfd.getCard()) {
                    case REQUIRED -> {
                        annotations.add(new Annotation(TypeRegistry.COLUMN_ANNOTATION, List.of(NULLABLE_FALSE)));
                    }
                    case REPEATED -> {
                        annotations.add(new Annotation(TypeRegistry.ELEMENT_COLLECTION_ANNOTATION));
                    }
                    case OPTIONAL -> {
                        annotations.add(new Annotation(TypeRegistry.COLUMN_ANNOTATION, List.of(NULLABLE_TRUE)));
                    }
                }
                Field f = new Field(this, mfd.getName(), mapping.getJavaType(), annotations);
                addField(f, oneOfCont);
                return;
            }
            String nestedId = messageType.getFullName();
            ConfigTable cfg = manager.getConfig(nestedId);
            if (cfg != null  && cfg.isAsProtobuf()) {
                mapping = new TypeMapping(SourceType.build(nestedId), new PrimitiveType("byte[]"));
                List<Annotation> annotations = new ArrayList<>(List.of(new Annotation(TypeRegistry.LOB_ANNOTATION)));
                switch (mfd.getCard()) {
                    case REQUIRED -> {
                        annotations.add(new Annotation(TypeRegistry.COLUMN_ANNOTATION, List.of(NULLABLE_FALSE)));
                    }
                    case REPEATED -> {
                        annotations.add(new Annotation(TypeRegistry.ELEMENT_COLLECTION_ANNOTATION));
                    }
                    case OPTIONAL -> {
                        annotations.add(new Annotation(TypeRegistry.COLUMN_ANNOTATION, List.of(NULLABLE_TRUE)));
                    }
                }
                Field f = new Field(this, mfd.getName(), mapping.getJavaType(), annotations);
                addField(f, oneOfCont);

                return;
            }
            String src = "nested#" + fullName();
            Table child = manager.getTable(nestedId, src);
            if(child == null) {
                logger.error("{} not configured", nestedId);
                return;
            }


            mapping = new TypeMapping(SourceType.build(nestedId), child.getJavaType());

            if (src.equals(child.createdBy) && isNestedType.test(nestedId)) {
                logger.error("Nested type {}", nestedId);
                // TODO add as a sub table?
                return;
            }

            List<Annotation> parentAnnotations = List.of();

            if(mfd.getCard()==Cardinality.REPEATED) mapping = mapping.toSet();

            // TODO if part of oneOf -> required goes to optional. child on owned -> optional
            Field parentField;
            Field childField = null;
            if(child.getConfig().isInsert()) {
                List<Annotation> childAnnotations = List.of();
                String childFieldName = CaseUtils.toLowerCamelCase(getSqlName() + "_" + mfd.getName());
                switch (mfd.getCard()) {
                    case REQUIRED -> {
                        parentAnnotations = List.of(new Annotation(TypeRegistry.ONE_TO_ONE_ANNOTATION, List.of("mappedBy = \"" + childFieldName + '"', LAZY_FETCH)));
                        childAnnotations = List.of(new Annotation(TypeRegistry.ONE_TO_ONE_ANNOTATION, List.of(LAZY_FETCH)));
                    }
                    case OPTIONAL -> {
                        parentAnnotations = List.of(new Annotation(TypeRegistry.ONE_TO_ONE_ANNOTATION, List.of("mappedBy = \"" + childFieldName + '"', OPTIONAL_TRUE, LAZY_FETCH)));
                        childAnnotations = List.of(new Annotation(TypeRegistry.ONE_TO_ONE_ANNOTATION, List.of(OPTIONAL_TRUE, LAZY_FETCH)));
                    }
                    case REPEATED -> {
                        parentAnnotations = List.of(new Annotation(TypeRegistry.ONE_TO_MANY_ANNOTATION, List.of("mappedBy = \"" + childFieldName + '"', LAZY_FETCH, CASCADE_ALL)));
                        childAnnotations = List.of(new Annotation(TypeRegistry.MANY_TO_ONE_ANNOTATION, List.of(LAZY_FETCH)));
                    }
                }
                childField = new Field(child, childFieldName, this.getJavaType(), childAnnotations);
            }else{
                parentAnnotations = switch (mfd.getCard()) {
                    case REQUIRED -> List.of(new Annotation(TypeRegistry.MANY_TO_ONE_ANNOTATION, List.of(LAZY_FETCH)));
                    case OPTIONAL -> List.of(new Annotation(TypeRegistry.MANY_TO_ONE_ANNOTATION, List.of(LAZY_FETCH, OPTIONAL_TRUE)));
                    case REPEATED -> List.of(new Annotation(TypeRegistry.MANY_TO_MANY_ANNOTATION, List.of(LAZY_FETCH)));
                };
            }
            DependencyManager.getInstance().addDependency(fullName(), child.fullName());
            parentField = new Field(this, mfd.getName(), mapping.getJavaType(), parentAnnotations);
            addField(parentField, oneOfCont);
            if(childField != null) {
                child.addMappedByField(parentField, childField);
            }


//            JavaType childFieldType = this.getJavaType();
//
//            List<Annotation> childAnnotations = new ArrayList<>(2);
//
//            List<Annotation> annotations = new ArrayList<>(2);
//            JavaType annotationMapping = TypeRegistry.ONE_TO_MANY_ANNOTATION;
//            childFieldType = new SetType(childFieldType);
//            switch (mfd.getCard()) {
//                case REQUIRED -> {
//                    annotations.add(new Annotation(TypeRegistry.MANY_TO_ONE_ANNOTATION, List.of(OPTIONAL_FALSE, LAZY_FETCH)));
//
//                    // CHILD ANNOTATIONS
//
//                }
//                case REPEATED -> {
//                    annotations.add(new Annotation(TypeRegistry.MANY_TO_MANY_ANNOTATION, List.of(LAZY_FETCH)));
//                    mapping = mapping.toSet();
//                    // CHILD ANNOTATIONS
//                    annotationMapping = TypeRegistry.MANY_TO_MANY_ANNOTATION;
//                }
//                case OPTIONAL -> {
//                    annotations.add(new Annotation(TypeRegistry.MANY_TO_ONE_ANNOTATION, List.of(OPTIONAL_TRUE, LAZY_FETCH)));
//                    // CHILD ANNOTATIONS
//                }
//            }
//            childAnnotations.add(new Annotation(annotationMapping, List.of("mappedBy = \"" + mfd.getName() + "\"", LAZY_FETCH)));
//            DependencyManager.getInstance().addDependency(fullName(), child.fullName());
////            child.addMappedByField(this);
//
//            Field f = new Field(this, mfd.getName(), mapping.getJavaType(), annotations);
//            addField(f, oneOfCont);
//
//            child.addMappedByField(f, new Field(child, getSqlName() + "_" + f.getName(), childFieldType, childAnnotations));
            return;
        }

        // Enum fields
        if (fd instanceof EnumFieldDescriptor efd) {
            Descriptors.EnumDescriptor ed = efd.getEnumType();
            JavaType enumType = new PrimitiveType(ed.getName());
            enums.put(enumType,
                    ed.getValues().stream()
                            .map(v -> Map.entry(v.getName(), v.getNumber()))
                            .sorted(Comparator.comparingInt(Map.Entry::getValue))
                            .collect(Collectors.toList()));

            List<Annotation> annotations = new ArrayList<>(List.of(new Annotation(TypeRegistry.ENUMERATED_ANNOTATION, List.of(TypeRegistry.ENUM_TYPE + ".ORDINAL"))));
            switch (efd.getCard()) {
                case REQUIRED -> {
                    annotations.add(new Annotation(TypeRegistry.COLUMN_ANNOTATION, List.of(NULLABLE_FALSE)));
                }
                case REPEATED -> {
                    logger.error("Not handling repeated enums");
                    return;
                }
                case OPTIONAL -> {
                    annotations.add(new Annotation(TypeRegistry.COLUMN_ANNOTATION, List.of(NULLABLE_TRUE)));
                }
            }

            Field f = new Field(this, efd.getName(), enumType, annotations);
            addField(f, oneOfCont);

            return;
        }

        // Primitive fields
        if (fd instanceof PrimitiveFieldDescriptor pfd) {
            TypeMapping type = TypeRegistry.getPrimitive(pfd.getType());

            List<Annotation> annotations = new ArrayList<>();
            switch (pfd.getCard()) {
                case REQUIRED -> {
                    annotations.add(new Annotation(TypeRegistry.COLUMN_ANNOTATION, List.of(NULLABLE_FALSE)));
                }
                case REPEATED -> {
                    type = type.toRepeated();
                    annotations.add(new Annotation(TypeRegistry.ELEMENT_COLLECTION_ANNOTATION));
                }
                case OPTIONAL -> {
                    annotations.add(new Annotation(TypeRegistry.COLUMN_ANNOTATION, List.of(NULLABLE_TRUE)));
                }
            }
            Field f = new Field(this, pfd.getName(), type.getJavaType(), annotations);
            addField(f, oneOfCont);

            return;
        }

        logger.error("Unhandled field descriptor type: {}", fd.getClass().getSimpleName());

        // TODO generate_if_missing
    }

    private String fullName() {
        return packageProvider.getPackage() + "." + name;
    }

    private OneOf addOneOf(String name, boolean required) {
        return oneOfs.computeIfAbsent(name, k -> new OneOf(name, required));
    }

    private void addField(Field f) {
        fields.put(f.getName(), f);
    }

    private void addField(Field f, OneOf oneOf) {
        if (oneOf != null) {
            List<Field> group = List.of(f);
            f.setOneOf(oneOf);
            f.setOneOfGroup(group);
            oneOf.getFields().add(group);
        }
        addField(f);
    }

    public void addMappedByField(Field other, Field myField) {
        addField(myField);
    }

    public String getSqlName() {
        return CaseUtils.toSnakeCase(name);
    }

    public String getName() {
        return name;
    }

    public Map<JavaType, List<Map.Entry<String, Integer>>> getEnums() {
        return enums;
    }

    public Set<String> getPrimaryKey() {
        return Collections.unmodifiableSet(primaryKey);
    }

    public Map<String, Field> getFields() {
        return Collections.unmodifiableMap(fields);
    }

    public ClassType getJavaType() {
        return new ClassType(packageProvider.getPackage(), name);
    }

    public ConfigTable getConfig() {
        return config;
    }

    @Override
    public String toString() {
        return "Table{" +
                "createdBy='" + createdBy + '\'' +
                ", manager=" + manager +
                ", config=" + config +
                ", name='" + name + '\'' +
                ", packageProvider=" + packageProvider +
                ", isNestedType=" + isNestedType +
                ", primaryKey=" + primaryKey +
                ", fields=" + fields +
//                ", foreignKeys=" + foreignKeys +
                ", oneOfs=" + oneOfs +
                ", enums=" + enums +
//                ", joinTables=" + joinTables +
                ", fieldDescriptors=" + fieldDescriptors +
                '}';
    }

    public static Table createRegularTable(
            TableManager manager,
            ConfigTable config,
            String name,
            Descriptors.Descriptor msg,
            String createdBy) {
        Map<String, AbstractFieldDescriptor> desc = new LinkedHashMap<>();
        msg.getFields().forEach(fd -> {
            ConfigField cf = config.getFields().getOrDefault(fd.getName(), new ConfigField());
            desc.put(fd.getName(), AbstractFieldDescriptor.build(fd, cf));
        });
        return new Table(manager, config, name,
                new FilePackageProvider(msg.getFile()), desc,
                createdBy, nested -> msg.getNestedTypes()
                .stream()
                .map(Descriptors.Descriptor::getFullName)
                .anyMatch(nested::equals));
    }
}
