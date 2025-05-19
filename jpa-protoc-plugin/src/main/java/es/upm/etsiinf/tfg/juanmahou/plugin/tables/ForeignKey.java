package es.upm.etsiinf.tfg.juanmahou.plugin.tables;

import es.upm.etsiinf.tfg.juanmahou.plugin.tables.field.Field;

import java.util.List;
import java.util.Objects;

/**
 * Represents a foreign key relationship between two tables.
 */
public class ForeignKey {
    private final List<Field> local;
    private final List<Field> foreign;

    public ForeignKey(List<Field> local, List<Field> foreign) {
        this.local = Objects.requireNonNull(local);
        this.foreign = Objects.requireNonNull(foreign);
    }

    public List<Field> getLocal() {
        return local;
    }

    public List<Field> getForeign() {
        return foreign;
    }
}
