package es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry;

import org.hl7.fhir.r4b.model.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@RequestScope
@Component
public class RequestGeneratorContext implements GeneratorContext {
    private Resource currentResource;

    public Resource getCurrentResource() {
        return currentResource;
    }

    public void setCurrentResource(Resource currentResource) {
        this.currentResource = currentResource;
    }
}
