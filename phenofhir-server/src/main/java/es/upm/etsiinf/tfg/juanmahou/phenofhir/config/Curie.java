package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Curie {
    public static class System {
        @NotEmpty
        private String system;
        private String version;

        public System() {
        }

        public System(String system, String version) {
            this.system = system;
            this.version = version;
        }



        public String getSystem() {
            return system;
        }

        public String getVersion() {
            return version;
        }

        public void setSystem(String system) {
            this.system = system;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            System system1 = (System) o;
            return Objects.equals(system, system1.system) && Objects.equals(version, system1.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(system, version);
        }

        @Override
        public String toString() {
            return "System{" +
                    "system='" + system + '\'' +
                    ", version='" + version + '\'' +
                    '}';
        }
    }
    @Valid
    private Map<String, List<System>> mapping = Map.of();

    public Map<String, List<System>> getMapping() {
        return mapping;
    }

    public void setMapping(Map<String, List<System>> mapping) {
        this.mapping = mapping;
    }
}
