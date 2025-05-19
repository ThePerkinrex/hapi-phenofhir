package es.upm.etsiinf.tfg.juanmahou.plugin.tables.field;

import es.upm.etsiinf.tfg.juanmahou.plugin.tables.Table;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.Annotation;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.JavaType;

import java.util.Objects;

/**
 * Represents a field in a table.
 */
public class Field {
    private final Table table;
    private final String name;
    private final JavaType type;
    private final Annotation[] annotations;

    public Field(Table table, String name, JavaType type, Annotation... annotations) {
        this.table = Objects.requireNonNull(table);
        this.name = Objects.requireNonNull(name);
        this.type = Objects.requireNonNull(type);
        this.annotations = annotations;
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

    public Annotation[] getAnnotations() {
        return annotations;
    }
}
