package es.upm.etsiinf.tfg.juanmahou.plugin.tables.field;

import com.google.protobuf.Descriptors.EnumDescriptor;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Cardinality;

import java.util.Objects;

public class EnumFieldDescriptor extends AbstractFieldDescriptor {
    private final EnumDescriptor enumType;

    public EnumFieldDescriptor(Cardinality card, String oneof, String name, boolean generated, EnumDescriptor enumType) {
        super(card, oneof, name, generated);
        this.enumType = Objects.requireNonNull(enumType);
    }

    public EnumDescriptor getEnumType() {
        return enumType;
    }

    @Override
    public EnumFieldDescriptor clone() {
        return new EnumFieldDescriptor(this.getCard(), this.getOneof(), this.getName(), this.isGenerated(), enumType);
    }
}
