package es.upm.etsiinf.tfg.juanmahou.plugin.types.source;

import java.util.Objects;

public class ProtoMessageSourceType implements SourceType {
    private final String message;

    public ProtoMessageSourceType(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ProtoMessageSourceType that = (ProtoMessageSourceType) o;
        return Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(message);
    }

    @Override
    public String toString() {
        return "ProtoMessageSourceType{" +
                "message='" + message + '\'' +
                '}';
    }
}
