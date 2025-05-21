package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import java.util.Map;

public class Config {
    private Curie curie;
    private Map<String, Mapping> mappings;

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
