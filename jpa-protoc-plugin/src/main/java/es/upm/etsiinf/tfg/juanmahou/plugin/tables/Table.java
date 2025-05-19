package es.upm.etsiinf.tfg.juanmahou.plugin.tables;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.FieldDescriptor;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.*;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.CaseUtils;
import es.upm.etsiinf.tfg.juanmahou.plugin.util.HashUtils;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Cardinality;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.ConfigCardinality;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.ConfigField;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.ConfigTable;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.field.*;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.providers.FilePackageProvider;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.providers.PackageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.stringtemplate.v4.ST;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Concrete implementation of a Table, managing fields, keys, and generation logic.
 */
public class Table {
    private static final Logger logger = LoggerFactory.getLogger(Table.class);

    private final String createdBy;
    private final TableManager manager;
    private final ConfigTable config;
    private final String name;
    private final PackageProvider packageProvider;
    private final Predicate<String> isNestedType;

    private final Set<String> primaryKey = new LinkedHashSet<>();
    private final Map<String, Field> fields = new LinkedHashMap<>();
    private final List<ForeignKey> foreignKeys = new ArrayList<>();
    private final Map<String, OneOf> oneOfs = new LinkedHashMap<>();
    private final Map<JavaType, List<Map.Entry<String, Integer>>> enums = new LinkedHashMap<>();
    private final List<Table> joinTables = new ArrayList<>();
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
            // TODO: add descriptor for synthetic if desired
        } else {
            throw new IllegalStateException("No primary key for " + fullName());
        }

        // process each field
        fieldDescriptors.values().forEach(this::processField);

        logger.info("> Finished {} as {}", fullName(), name);
    }

    private void processField(AbstractFieldDescriptor fd) {
        boolean generated = fd.isGenerated();
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

            ConfigField keyCfg = new ConfigField(); keyCfg.setCardinality(ConfigCardinality.REQUIRED);
            ConfigField valCfg = new ConfigField(); valCfg.setCardinality(ConfigCardinality.REQUIRED);
            AbstractFieldDescriptor keyDesc = AbstractFieldDescriptor.build(keyPb, keyCfg);
            AbstractFieldDescriptor valDesc = AbstractFieldDescriptor.build(valPb, valCfg);

            // build child config
            logger.error("Map field {} not handled", fd.getName());
            return;
//            Map<String, ConfigField> childCfg = new LinkedHashMap<>();
//            childCfg.put("key", keyCfg);
//            childCfg.put("value", valCfg);
//            Map<String, AbstractFieldDescriptor> fieldDescriptors = new LinkedHashMap<>();
//            fieldDescriptors.put("key", keyDesc);
//            fieldDescriptors.put("value", valDesc);
//            for (String pkName : getPrimaryKey()) {
//                String n = getSqlName() + "_" + pkName;
//                ConfigField c = new ConfigField(); c.setCardinality(ConfigCardinality.REQUIRED);
//                childCfg.put(n, c);
//                AbstractFieldDescriptor afd = this.fieldDescriptors.get(pkName).clone();
//                afd.setName(n);
//                afd.setGenerated(false);
//                fieldDescriptors.put(n, afd);
//            }

            // TODO: build descriptors and instantiate child Table via createRegularTable
//            return;
        }

        // Message fields
        if (fd instanceof MessageFieldDescriptor) {
//            MessageFieldDescriptor mfd = (MessageFieldDescriptor) fd;
//            String nestedId = mfd.getMessageType().getFullName();
//            TypeMapping tm = TypeRegistry.getMessage(nestedId);
//            if (tm != null) {
//                addField(new Field(this, fd.getName(), tm,
//                        mfd.getCard() == Cardinality.OPTIONAL, extras));
//                if (oneOfCont!=null) oneOfCont.getFieldGroups().add(Collections.singletonList(fields.get(fd.getName())));
//                return;
//            }
            // nested and repeated handlers omitted for brevity...
            logger.error("Message field {} not handled", fd.getName());
            return;
        }

        // Enum fields
        if (fd instanceof EnumFieldDescriptor) {
//            EnumFieldDescriptor efd = (EnumFieldDescriptor) fd;
//            if (efd.getCard() == Cardinality.REQUIRED || efd.getCard() == Cardinality.OPTIONAL) {
//                EnumDescriptor ed = efd.getEnumType();
//                JavaType enumType = new JavaType(ed.getFullName(), ed.getName());
//                enums.put(enumType,
//                        ed.getValues().stream()
//                                .map(v -> Map.entry(v.getName(), v.getNumber()))
//                                .collect(Collectors.toList()));
//                Field f = new Field(this, fd.getName(),
//                        new TypeMapping(null, ed.getFullName(), enumType),
//                        efd.getCard() == Cardinality.OPTIONAL, extras);
//                f.setEnum(true);
//                addField(f, true);
//                return;
//            }
            logger.error("Enum field {} not handled", fd.getName());
            return;
        }

        // Primitive fields
        if (fd instanceof PrimitiveFieldDescriptor) {
//            PrimitiveFieldDescriptor pfd = (PrimitiveFieldDescriptor) fd;
//            JavaType jt = TypeRegistry.PRIMITIVE_MAP.get(pfd.getType()).getJavaType();
//            Field f = new Field(this, pfd.getName(), jt,
//                    pfd.getCard() == Cardinality.OPTIONAL, extras);
//            addField(f);
//            if (oneOfCont!=null) oneOfCont.getFieldGroups().add(Collections.singletonList(f));
//            return;
            logger.error("Primitive field {} not handled", fd.getName());
            return;
        }

        logger.error("Unhandled field descriptor type: {}", fd.getClass().getSimpleName());
    }

    private String fullName() {
        return packageProvider.getPackage() + "." + name;
    }

    private void addJoinTable(Table t) {
        joinTables.add(t);
    }

    private OneOf addOneOf(String name, boolean required) {
        return oneOfs.computeIfAbsent(name, k -> new OneOf(name, required));
    }

    private void addField(Field f) {
        fields.put(f.getName(), f);
    }

    private void addForeignKey(ForeignKey fk, Table t) {
        foreignKeys.add(fk);
    }

    public String getSqlName() {
        return CaseUtils.toSnakeCase(name);
    }

    public String getName() { return name; }
    public Set<String> getPrimaryKey() { return Collections.unmodifiableSet(primaryKey); }
    public Map<String, Field> getFields() { return Collections.unmodifiableMap(fields); }
    public List<ForeignKey> getForeignKeys() { return Collections.unmodifiableList(foreignKeys); }
    public Map<String, OneOf> getOneOfs() { return Collections.unmodifiableMap(oneOfs); }
    public List<Table> getJoinTables() { return Collections.unmodifiableList(joinTables); }
    public Field getField(String name) { return fields.get(name); }
    public ClassType getJavaType() { return new ClassType(packageProvider.getPackage(), name); }

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
                ", foreignKeys=" + foreignKeys +
                ", oneOfs=" + oneOfs +
                ", enums=" + enums +
                ", joinTables=" + joinTables +
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
