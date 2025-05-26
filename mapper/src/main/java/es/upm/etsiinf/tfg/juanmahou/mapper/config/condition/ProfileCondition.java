package es.upm.etsiinf.tfg.juanmahou.mapper.config.condition;

import ca.uhn.fhir.rest.server.exceptions.UnprocessableEntityException;
import jakarta.validation.constraints.NotEmpty;
import org.hl7.fhir.instance.model.api.IBaseResource;

public class ProfileCondition extends SourceCondition {
    @NotEmpty
    private String profile;

    public String getProfile() {
        return profile;
    }

    public void setProfile(String profile) {
        this.profile = profile;
    }

    @Override
    public Exception getException(Object o) {
        return new UnprocessableEntityException("The provided object does not include the profile " + profile + ": " + o);
    }

    @Override
    public boolean check(Object o) {
        return o instanceof IBaseResource r && r.getMeta().getProfile().stream().anyMatch(p -> p.getValueAsString().equals(getProfile()));
    }
}
