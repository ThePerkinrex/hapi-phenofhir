package es.upm.etsiinf.tfg.juanmahou.plugin.tables.field;

import com.google.protobuf.Descriptors;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Cardinality;

import java.util.Objects;

public class MapFieldDescriptor extends AbstractFieldDescriptor {
    private final Descriptors.Descriptor messageType;

    public MapFieldDescriptor(Cardinality card, String oneof, String name, boolean generated, Descriptors.Descriptor messageType) {
        super(card, oneof, name, generated);
        this.messageType = Objects.requireNonNull(messageType);
    }

    public Descriptors.Descriptor getMessageType() {
        return messageType;
    }


    @Override
    public MapFieldDescriptor clone() {
        return new MapFieldDescriptor(this.getCard(), this.getOneof(), this.getName(), this.isGenerated(), messageType);
    }
}
