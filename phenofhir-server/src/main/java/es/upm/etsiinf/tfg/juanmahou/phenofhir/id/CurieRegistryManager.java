package es.upm.etsiinf.tfg.juanmahou.phenofhir.id;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

@RequestScope
@Component
public class CurieRegistryManager {
    public class CurieRegistry {
        Set<String> curies = new ConcurrentSkipListSet<>();

        public Set<String> getCuries() {
            return Set.copyOf(curies);
        }
    }

    private CurieRegistry registry = null;

    public CurieRegistry getRegistry() {
        if(registry == null) throw new NullPointerException();
        return registry;
    }

    public void newRegistry() {
        this.registry = new CurieRegistry();
    }

    public void registerCurie(String curie) {
        if(this.registry != null) this.registry.curies.add(curie);
    }

    public void merge(CurieRegistry other) {
        if(this.registry != null && other != null) this.registry.curies.addAll(other.curies);
    }



    public void clearRegistry() {
        this.registry = null;
    }
}
