package es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.GeneralPhenomicResources;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4b.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@RequestScope
@Component
public class Resolver {
    public static class Resolved {
        private final Resource resource;
        private Object pheno;

        private List<Consumer<Object>> onBuild = new ArrayList<>();

        public Resolved(Resource resource) {
            this.resource = resource;
            this.pheno = null;
        }

        public Resource getResource() {
            return resource;
        }

        public Object getPheno() {
            return pheno;
        }

        public void setPheno(Object pheno) {
            this.pheno = pheno;
            for(var r : onBuild) {
                r.accept(pheno);
            }
            this.onBuild.clear();
        }

        public void onBuild(Consumer<Object> consumer) {
            if(this.pheno == null) {
                this.onBuild.add(consumer);
            }else{
                consumer.accept(this.pheno);
            }
        }

        @Override
        public String toString() {
            return "Resolved{" +
                    "resource=" + resource +
                    ", pheno=" + pheno +
                    ", onBuild=" + onBuild +
                    '}';
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

    public Resolved register(String id, Resolved res) {
        resources.put(id, res);
        return res;
    }

    public Resolved register(String id, Resource res) {
        return register(id, new Resolved(res));
    }

    public Resolved get(String id) {
        Resolved res = resources.get(id);
        if(res != null) return res;
//        var gpr = generalPhenomicResources.get()
        return null;
    }
}
