package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Map;

public class Config {
    @Valid
    private Curie curie = new Curie();
    @Valid
    private Map<String, Mapping> mappings = Map.of();

    public OwnIdentifiers getOwnIdentifiers() {
        return ownIdentifiers;
    }

    public void setOwnIdentifiers(OwnIdentifiers ownIdentifiers) {
        this.ownIdentifiers = ownIdentifiers;
    }

    @NotNull
    private OwnIdentifiers ownIdentifiers;

    public Map<String, Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(Map<String, Mapping> mappings) {
        this.mappings = mappings;
    }

    public Curie getCurie() {
        return curie;
    }

    public void setCurie(Curie curie) {
        this.curie = curie;
    }

    @Override
    public String toString() {
        return "Config{" +
                "curie=" + curie +
                ", mappings=" + mappings +
                '}';
    }
}
