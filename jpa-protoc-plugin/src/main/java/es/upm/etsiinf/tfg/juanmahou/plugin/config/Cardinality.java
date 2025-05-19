package es.upm.etsiinf.tfg.juanmahou.plugin.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum Cardinality {
    REQUIRED("required"),
    REPEATED("repeated"),
    OPTIONAL("optional");

    private final String value;

    Cardinality(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static Cardinality fromValue(String value) {
        for (Cardinality c : values()) {
            if (c.value.equals(value)) {
                return c;
            }
        }
        throw new IllegalArgumentException("Unknown Cardinality: " + value);
    }
}