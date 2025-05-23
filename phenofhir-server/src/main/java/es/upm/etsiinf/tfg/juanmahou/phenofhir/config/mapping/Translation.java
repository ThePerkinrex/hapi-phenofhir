package es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping;

import jakarta.validation.constraints.NotEmpty;

public class Translation {

    @NotEmpty
    private String phenoName;

    @NotEmpty
    private String fhirName;
    private String description;

    private String mapper;

    public String getPhenoName() {
        return phenoName;
    }

    public void setPhenoName(String phenoName) {
        this.phenoName = phenoName;
    }

    public String getFhirName() {
        return fhirName;
    }

    public void setFhirName(String fhirName) {
        this.fhirName = fhirName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMapper() {
        return mapper;
    }

    public void setMapper(String mapper) {
        this.mapper = mapper;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "phenoName='" + phenoName + '\'' +
                ", fhirName='" + fhirName + '\'' +
                ", description='" + description + '\'' +
                ", mapper='" + mapper + '\'' +
                '}';
    }
}
