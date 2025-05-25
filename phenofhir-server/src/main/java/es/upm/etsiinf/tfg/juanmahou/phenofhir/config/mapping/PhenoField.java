package es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.constraint.IsGenerator;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class PhenoField {
    @NotEmpty
    private String name;

    @NotNull
    @Valid
    @IsGenerator
    private PhenoGenerator generator;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public PhenoGenerator getGenerator() {
        return generator;
    }

    public void setGenerator(PhenoGenerator generator) {
        this.generator = generator;
    }
}
