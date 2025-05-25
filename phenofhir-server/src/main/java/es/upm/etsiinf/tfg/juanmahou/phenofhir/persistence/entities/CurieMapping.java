package es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.entities;

import jakarta.persistence.*;

@Entity
public class CurieMapping {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Long id;

    @Column(nullable = false)
    private String curie;

    @Column(nullable = false)
    private String system;

    private String version;

    protected CurieMapping() {}

    public CurieMapping(String curie, String system, String version) {
        this.curie = curie;
        this.system = system;
        this.version = version;
    }

    public CurieMapping(String curie, String system) {
        this.curie = curie;
        this.system = system;
    }

    public Long getId() {
        return id;
    }

    public String getCurie() {
        return curie;
    }

    public String getSystem() {
        return system;
    }

    public String getVersion() {
        return version;
    }
}
