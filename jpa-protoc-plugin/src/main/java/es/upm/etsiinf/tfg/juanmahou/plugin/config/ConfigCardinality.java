package es.upm.etsiinf.tfg.juanmahou.plugin.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.function.Supplier;

public enum ConfigCardinality {
    REQUIRED("required"),
    REPEATED("repeated"),
    OPTIONAL("optional"),
    INHERIT("inherit");

    private final String value;

    ConfigCardinality(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static ConfigCardinality fromValue(String value) {
        for (ConfigCardinality c : values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown ConfigCardinality: " + value);
    }

    /**
     * Resolve an inherited cardinality or return the explicit one.
     */
    public Cardinality resolve(Supplier<Cardinality> other) {
        if (this == INHERIT) {
            return other.get();
        } else {
            return Cardinality.fromValue(this.value);
        }
    }
}

