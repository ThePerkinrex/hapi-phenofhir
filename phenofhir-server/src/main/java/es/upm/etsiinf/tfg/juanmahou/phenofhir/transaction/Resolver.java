package es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction;

import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.CurieRegistryManager;
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
        private WithId<?> pheno;
        private CurieRegistryManager.CurieRegistry registry;
        private final CurieRegistryManager curieRegistryManager;

        private List<Consumer<Object>> onBuild = new ArrayList<>();

        public Resolved(Resource resource, CurieRegistryManager curieRegistryManager) {
            this.resource = resource;
            this.curieRegistryManager = curieRegistryManager;
            this.pheno = null;
            this.registry = null;
        }

        public Resource getResource() {
            return resource;
        }

        public WithId<?> getPheno() {
            return pheno;
        }

        public void setPheno(WithId<?> pheno) {
            this.pheno = pheno;
            for(var r : onBuild) {
                r.accept(pheno);
            }
            this.onBuild.clear();
            this.registry = curieRegistryManager.getRegistry();
        }

        public CurieRegistryManager.CurieRegistry getRegistry() {
            return registry;
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
    private final CurieRegistryManager curieRegistryManager;

    public Resolver(GeneralPhenomicResources generalPhenomicResources, CurieRegistryManager curieRegistryManager) {
        this.curieRegistryManager = curieRegistryManager;
        log.info("Building resolver");
        resources = new HashMap<>();
        this.generalPhenomicResources = generalPhenomicResources;

    }

    public Resolved register(String id, Resolved res) {
        resources.put(id, res);
        return res;
    }

    public Resolved register(String id, Resource res) {
        return register(id, new Resolved(res, curieRegistryManager));
    }

    public Resolved get(String id) {
        Resolved res = resources.get(id);
        if(res != null) return res;
//        var gpr = generalPhenomicResources.get()
        return null;
    }
}
