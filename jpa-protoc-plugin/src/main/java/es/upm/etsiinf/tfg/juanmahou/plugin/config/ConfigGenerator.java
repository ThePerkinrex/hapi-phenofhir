package es.upm.etsiinf.tfg.juanmahou.plugin.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class ConfigGenerator {
    private String generator;

    @JsonProperty("parameters")
    private Map<String, Object> params = Map.of();

    public String getGenerator() {
        return generator;
    }

    public void setGenerator(String generator) {
        if(generator == null) throw new IllegalArgumentException("generator is required");
        this.generator = generator;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    @Override
    public String toString() {
        return "ConfigGenerator{" +
                "generator='" + generator + '\'' +
                ", params=" + params +
                '}';
    }
}
