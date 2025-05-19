package es.upm.etsiinf.tfg.juanmahou.phenofhir;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.annotation.RequiredParam;
import ca.uhn.fhir.rest.annotation.Search;
import ca.uhn.fhir.rest.api.server.IBundleProvider;
import ca.uhn.fhir.rest.param.StringParam;
import ca.uhn.fhir.rest.server.IResourceProvider;
import ca.uhn.fhir.rest.server.SimpleBundleProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Patient;
import org.hl7.fhir.r4.model.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Component
public class PatientResourceProvider implements IResourceProvider {
    @Override
    public Class<? extends IBaseResource> getResourceType() {
        return Patient.class;
    }

    @Read
    public Patient getPatientById(@IdParam IdType id) {
        // for demo, just return a dummy Patient
        Patient p = new Patient();
        p.setId(id);
        p.addName().setFamily("Doe").addGiven("John");
        return p;
    }

    @Search
    public IBundleProvider searchByFamily(@RequiredParam(name = Patient.SP_FAMILY) StringParam family) {
        Patient p = new Patient();
        p.setId(new IdType("Patient", "1"));
        p.addName().setFamily(family.getValue()).addGiven("Test");
        List<Patient> list = Collections.singletonList(p);
        return new SimpleBundleProvider(list);
    }
}
