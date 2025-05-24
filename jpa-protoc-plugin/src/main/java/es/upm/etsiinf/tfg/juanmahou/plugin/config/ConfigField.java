package es.upm.etsiinf.tfg.juanmahou.plugin.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigField {
    private ConfigCardinality cardinality = ConfigCardinality.INHERIT;

    @JsonProperty("generate_if_missing")
    private ConfigGenerator generator;

    public ConfigCardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(ConfigCardinality cardinality) {
        this.cardinality = cardinality;
    }

    public ConfigGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(ConfigGenerator generator) {
        this.generator = generator;
    }

    @Override
    public String toString() {
        return "ConfigField{" +
                "cardinality=" + cardinality +
                ", generator=" + generator +
                '}';
    }
}
