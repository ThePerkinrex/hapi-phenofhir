package es.upm.etsiinf.tfg.juanmahou.mapper.config;

import es.upm.etsiinf.tfg.juanmahou.mapper.config.condition.SourceCondition;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.field.Field;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;

public class Mapping {
    @NotEmpty
    private String source;
    @NotEmpty
    private String target;

    @Valid
    private List<SourceCondition> sourceConditions = List.of();

    @Valid
    private Map<String, Field> fields = Map.of();

    public Map<String, Field> getFields() {
        return fields;
    }

    public void setFields(Map<String, Field> fields) {
        this.fields = fields;
    }


    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<SourceCondition> getSourceConditions() {
        return sourceConditions;
    }

    public void setSourceConditions(List<SourceCondition> sourceConditions) {
        this.sourceConditions = sourceConditions;
    }
}
