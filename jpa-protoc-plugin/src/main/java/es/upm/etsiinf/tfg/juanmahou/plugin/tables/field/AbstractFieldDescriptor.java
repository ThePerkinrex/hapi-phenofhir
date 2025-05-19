package es.upm.etsiinf.tfg.juanmahou.plugin.tables.field;

import com.google.protobuf.Descriptors;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Cardinality;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.ConfigField;

import java.util.Objects;

public abstract class AbstractFieldDescriptor implements Cloneable{
    private final Cardinality card;
    private final String oneof;
    private String name;
    private boolean generated;

    protected AbstractFieldDescriptor(Cardinality card, String oneof, String name, boolean generated) {
        this.card = Objects.requireNonNull(card);
        this.oneof = oneof;
        this.name = Objects.requireNonNull(name);
        this.generated = generated;
    }

    public Cardinality getCard() {
        return card;
    }

    public String getOneof() {
        return oneof;
    }

    public String getName() {
        return name;
    }

    public boolean isGenerated() {
        return generated;
    }

    public void setGenerated(boolean generated) {
        this.generated = generated;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public abstract AbstractFieldDescriptor clone();

    public static AbstractFieldDescriptor build(Descriptors.FieldDescriptor field, ConfigField config) {
        Cardinality card;
        if (config.getCardinality() == null) {
            card = (field.isRepeated() ? Cardinality.REPEATED : Cardinality.OPTIONAL);
        } else {
            card = config.getCardinality().resolve(
                    () -> field.isRepeated() ? Cardinality.REPEATED : Cardinality.OPTIONAL
            );
        }
        String oneof = (field.getContainingOneof() != null)
                ? field.getContainingOneof().getName()
                : null;
        boolean generated = false;


        if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
            if (field.isMapField()) {
                return new MapFieldDescriptor(card, oneof, field.getName(), generated, field.getMessageType());
            } else {
                return new MessageFieldDescriptor(card, oneof, field.getName(), generated, field.getMessageType());
            }
        } else if (field.getJavaType() == Descriptors.FieldDescriptor.JavaType.ENUM) {
            return new EnumFieldDescriptor(card, oneof, field.getName(), generated, field.getEnumType());
        } else {
            return new PrimitiveFieldDescriptor(card, oneof, field.getName(), generated, field.getJavaType());
        }
    }
}
