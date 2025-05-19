package es.upm.etsiinf.tfg.juanmahou.plugin.tables.field;

import com.google.protobuf.Descriptors;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Cardinality;

public class PrimitiveFieldDescriptor extends AbstractFieldDescriptor {
    private final Descriptors.FieldDescriptor.JavaType type;

    public PrimitiveFieldDescriptor(Cardinality card, String oneof, String name, boolean generated, Descriptors.FieldDescriptor.JavaType type) {
        super(card, oneof, name, generated);
        this.type = type;
    }

    public Descriptors.FieldDescriptor.JavaType getType() {
        return type;
    }



    @Override
    public PrimitiveFieldDescriptor clone() {
        return new PrimitiveFieldDescriptor(this.getCard(), this.getOneof(), this.getName(), this.isGenerated(), type);
    }
}
