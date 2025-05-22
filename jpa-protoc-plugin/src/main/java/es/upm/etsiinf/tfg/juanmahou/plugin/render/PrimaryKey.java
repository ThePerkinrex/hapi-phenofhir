package es.upm.etsiinf.tfg.juanmahou.plugin.render;

import es.upm.etsiinf.tfg.juanmahou.plugin.tables.field.Field;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.Annotation;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.TypeRegistry;

import java.util.List;

public class PrimaryKey {
    public Annotation embeddable = new Annotation(TypeRegistry.EMBEDDABLE_ANNOTATION);

    public List<Field> fields;
    public List<Accessor> accessors;

    public PrimaryKey(List<Field> fields, List<Accessor> accessors) {
        this.fields = fields;
        this.accessors = accessors;
    }
}
