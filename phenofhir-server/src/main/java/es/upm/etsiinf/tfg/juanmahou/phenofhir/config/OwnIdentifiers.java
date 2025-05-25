package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import jakarta.validation.constraints.NotEmpty;

public class OwnIdentifiers {
    @NotEmpty
    private String system;
    private String version;
    @NotEmpty
    private String curie;

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getCurie() {
        return curie;
    }

    public void setCurie(String curie) {
        this.curie = curie;
    }
}
