package es.upm.etsiinf.tfg.juanmahou.plugin.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConfigField {
    @JsonProperty("as_fk_fields")
    private Map<String, String> asFkFields;

    private ConfigCardinality cardinality = ConfigCardinality.INHERIT;

    public Map<String, String> getAsFkFields() {
        return asFkFields;
    }

    public void setAsFkFields(Map<String, String> asFkFields) {
        this.asFkFields = asFkFields;
    }

    public ConfigCardinality getCardinality() {
        return cardinality;
    }

    public void setCardinality(ConfigCardinality cardinality) {
        this.cardinality = cardinality;
    }

    @Override
    public String toString() {
        return "ConfigField{" +
                "asFkFields=" + asFkFields +
                ", cardinality=" + cardinality +
                '}';
    }
}
