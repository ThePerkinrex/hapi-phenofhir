package es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping;

import com.fasterxml.jackson.annotation.JsonIgnore;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.IGenerator;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class PhenoGenerator {
    @NotEmpty
    private String name;

    @NotNull
    private Map<String, Object> params = Map.of();

    @JsonIgnore
    private IGenerator.ConfiguredGenerator<?, ?> generator;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

    public IGenerator.ConfiguredGenerator<?, ?> getGenerator() {
        return generator;
    }

    public void setGenerator(IGenerator.ConfiguredGenerator<?, ?> generator) {
        this.generator = generator;
    }
}
