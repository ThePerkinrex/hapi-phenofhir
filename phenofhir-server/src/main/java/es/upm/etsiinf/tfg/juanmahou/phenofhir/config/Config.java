package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.Mapping;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

public class Config {
    @Valid
    private Curie curie = new Curie();
    @Valid
    private List<Mapping> mappings = List.of();

    public OwnIdentifiers getOwnIdentifiers() {
        return ownIdentifiers;
    }

    public void setOwnIdentifiers(OwnIdentifiers ownIdentifiers) {
        this.ownIdentifiers = ownIdentifiers;
    }

    @NotNull
    private OwnIdentifiers ownIdentifiers;

    public List<Mapping> getMappings() {
        return mappings;
    }

    public void setMappings(List<Mapping> mappings) {
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
