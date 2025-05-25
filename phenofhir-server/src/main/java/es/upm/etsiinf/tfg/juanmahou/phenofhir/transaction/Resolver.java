package es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.GeneralPhenomicResources;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.HashMap;
import java.util.Map;

@RequestScope
@Component
public class Resolver {
    public record Resolved(Resource resource, Object pheno) {
        public Resolved(Resource resource) {
            this(resource, null);
        }
    }
    private static final Logger log = LoggerFactory.getLogger(Resolver.class);

    private final Map<String, Resolved> resources;
    private final GeneralPhenomicResources generalPhenomicResources;

    public Resolver(GeneralPhenomicResources generalPhenomicResources) {
        log.info("Building resolver");
        resources = new HashMap<>();
        this.generalPhenomicResources = generalPhenomicResources;

    }

    public void register(String id, Resolved res) {
        resources.put(id, res);
    }

    public void register(String id, Resource res) {
        register(id, new Resolved(res));
    }

    public Resolved get(String id) {
        Resolved res = resources.get(id);
        if(res != null) return res;
//        var gpr = generalPhenomicResources.get()
        return null;
    }
}
