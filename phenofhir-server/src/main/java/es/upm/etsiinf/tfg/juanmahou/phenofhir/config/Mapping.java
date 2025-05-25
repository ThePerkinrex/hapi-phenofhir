package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.constraint.IsClass;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.PhenoField;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;


import java.util.List;

public class Mapping {
    private String profile;

    @IsClass
    private String target;

    @Valid
    @NotNull
    private List<Translation> translations = List.of();

    @Override
    public String toString() {
        return "Mapping{" +
                "profile='" + profile + '\'' +
                ", target='" + target + '\'' +
                ", translations=" + translations +
                ", phenoFields=" + phenoFields +
                '}';
    }

    public List<PhenoField> getPhenoFields() {
        return phenoFields;
    }

    public void setPhenoFields(List<PhenoField> phenoFields) {
        this.phenoFields = phenoFields;
    }

    @Valid
    @NotNull
    private List<PhenoField> phenoFields = List.of();

    public List<Translation> getTranslations() {
        return translations == null ? List.of() : translations;
    }

    public void setTranslations(List<Translation> translations) {
        this.translations = translations;
    }

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

}
