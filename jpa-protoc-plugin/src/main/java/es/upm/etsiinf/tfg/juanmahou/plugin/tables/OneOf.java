package es.upm.etsiinf.tfg.juanmahou.plugin.tables;

import es.upm.etsiinf.tfg.juanmahou.plugin.tables.field.Field;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a SQL CHECK or similar one-of constraint.
 */
public class OneOf {
    private final String name;
    private final boolean required;
    private final List<List<Field>> fields = new ArrayList<>();

    public OneOf(String name, boolean required) {
        this.name = Objects.requireNonNull(name);
        this.required = required;
    }

    public String getName() {
        return name;
    }

    public boolean isRequired() {
        return required;
    }

    public List<List<Field>> getFields() {
        return fields;
    }
}
