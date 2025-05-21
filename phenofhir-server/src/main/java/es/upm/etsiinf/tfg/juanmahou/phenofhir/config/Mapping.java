package es.upm.etsiinf.tfg.juanmahou.phenofhir.config;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation;

import java.util.List;

public class Mapping {
    private String profile;

    private String target;

    private List<Translation> translations;

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

    @Override
    public String toString() {
        return "Mapping{" +
                "profile='" + profile + '\'' +
                ", target='" + target + '\'' +
                ", translations=" + translations +
                '}';
    }
}
