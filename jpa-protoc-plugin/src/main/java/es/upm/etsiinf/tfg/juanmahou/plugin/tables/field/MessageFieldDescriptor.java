package es.upm.etsiinf.tfg.juanmahou.plugin.tables.field;

import com.google.protobuf.Descriptors.Descriptor;
import es.upm.etsiinf.tfg.juanmahou.plugin.config.Cardinality;

import java.util.Objects;

public class MessageFieldDescriptor extends AbstractFieldDescriptor {
    private final Descriptor messageType;

    public MessageFieldDescriptor(Cardinality card, String oneof, String name, boolean generated, Descriptor messageType) {
        super(card, oneof, name, generated);
        this.messageType = Objects.requireNonNull(messageType);
    }

    public Descriptor getMessageType() {
        return messageType;
    }



    @Override
    public MessageFieldDescriptor clone() {
        return new MessageFieldDescriptor(this.getCard(), this.getOneof(), this.getName(), this.isGenerated(), messageType);
    }
}
