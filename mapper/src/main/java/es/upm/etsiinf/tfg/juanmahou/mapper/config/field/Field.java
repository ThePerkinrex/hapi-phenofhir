package es.upm.etsiinf.tfg.juanmahou.mapper.config.field;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class Field {
    @NotNull
    private List<String> sources;

    @NotEmpty
    private String mapper = "default";

    public List<String> getSources() {
        return sources;
    }

    public void setSources(List<String> sources) {
        this.sources = sources;
    }

    public String getMapper() {
        return mapper;
    }

    public void setMapper(String mapper) {
        this.mapper = mapper;
    }
}
