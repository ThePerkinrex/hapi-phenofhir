package es.upm.etsiinf.tfg.juanmahou.plugin.tables.field;

import es.upm.etsiinf.tfg.juanmahou.plugin.tables.OneOf;
import es.upm.etsiinf.tfg.juanmahou.plugin.tables.Table;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.Annotation;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.JavaType;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a field in a table.
 */
public class Field {
    private final Table table;
    private final String name;
    private final JavaType type;
    private final List<Annotation> annotations;
    private OneOf oneOf;
    private List<Field> oneOfGroup;
    private String defaultValue = "";

    public Field(Table table, String name, JavaType type, List<Annotation> annotations) {
        this.table = Objects.requireNonNull(table);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.annotations = annotations;
    }

    public Field(Table table, String name, JavaType type, List<Annotation> annotations, String defaultValue) {
        this(table, name, type, annotations);
        this.setDefaultValue(defaultValue);
    }

    public Table getTable() {
        return table;
    }

    public String getName() {
        return name;
    }

    public JavaType getType() {
        return type;
    }

    public List<Annotation> getAnnotations() {
        return Collections.unmodifiableList(annotations);
    }

    public OneOf getOneOf() {
        return oneOf;
    }

    public void setOneOf(OneOf oneOf) {
        this.oneOf = oneOf;
    }

    public List<Field> getOneOfGroup() {
        return oneOfGroup;
    }

    public void setOneOfGroup(List<Field> oneOfGroup) {
        this.oneOfGroup = oneOfGroup;
    }

    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = " = " + defaultValue;
    }
}
