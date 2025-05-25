package es.upm.etsiinf.tfg.juanmahou.mapper.config.condition;

import jakarta.validation.constraints.NotEmpty;

public class ProfileCondition extends SourceCondition {
    @NotEmpty
    private String profile;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }
}
