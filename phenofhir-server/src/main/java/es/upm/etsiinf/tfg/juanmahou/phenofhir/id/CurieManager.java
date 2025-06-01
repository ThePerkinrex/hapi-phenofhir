package es.upm.etsiinf.tfg.juanmahou.phenofhir.id;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.entities.CurieMapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.CurieMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class CurieManager {
    private static final Logger log = LoggerFactory.getLogger(CurieManager.class);

    public record System(String system, String version) {
        public System(String system) {
            this(system, null);
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            System system1 = (System) o;
            return Objects.equals(system, system1.system) && Objects.equals(version, system1.version);
        }

        @Override
        public int hashCode() {
            return Objects.hash(system, version);
        }
    }

    private final ObjectProvider<CurieRegistryManager> curieRegistryManager;

    private final CurieMappingRepository curieMappingRepository;
    private final Map<String, System> systemForCurieCache;
    private final Map<System, String> curieForSystemCache;
    private final Config config;

    public CurieManager(ObjectProvider<CurieRegistryManager> curieRegistryManager, CurieMappingRepository curieMappingRepository, Config config) {
        this.curieRegistryManager = curieRegistryManager;
        this.curieMappingRepository = curieMappingRepository;
        this.curieForSystemCache = new HashMap<>();
        this.systemForCurieCache = new HashMap<>();
        curieForSystemCache.put(new System(config.getOwnIdentifiers().getSystem(), config.getOwnIdentifiers().getVersion()), config.getOwnIdentifiers().getCurie());
        systemForCurieCache.put(config.getOwnIdentifiers().getCurie(), new System(config.getOwnIdentifiers().getSystem(), config.getOwnIdentifiers().getVersion()));
        for (var c : config.getCurie().getMapping().entrySet()) {
            var curie = c.getKey();
            for (var s : c.getValue()) {
                var system = new System(s.getSystem(), s.getVersion());
                curieForSystemCache.put(system, curie);
                systemForCurieCache.put(curie, system);
            }
        }
        this.config = config;
    }


    public String getCurieForSystem(System system) {
        var cached = curieForSystemCache.get(system);
        if(cached != null) {
            registerCurie(cached);
            return cached;
        }
        CurieMapping mapping;
        if (system.version() != null) {
            mapping = curieMappingRepository.findBySystemAndVersion(system.system(), system.version());
        }else{
            mapping = curieMappingRepository.findBySystem(system.system());
        }
        if(mapping == null) {
            mapping = new CurieMapping(config.getOwnIdentifiers().getCurie()+"-"+ UUID.randomUUID().toString().toLowerCase(), system.system, system.version);
            mapping = curieMappingRepository.save(mapping);
        }

        registerCurie(mapping.getCurie());
        return mapping.getCurie();
    }


    public System getSystemForCurie(String curie) {
        registerCurie(curie);
        var cached = systemForCurieCache.get(curie);
        if(cached != null) return cached;

        List<CurieMapping> mappings = curieMappingRepository.findByCurie(curie);
        if(mappings == null || mappings.isEmpty()) throw new RuntimeException("No mapping found for curie " + curie);
        CurieMapping mapping = mappings.getFirst();
        return new System(mapping.getSystem(), mapping.getVersion());
    }

    public System getOwnSystem() {
        registerCurie(config.getOwnIdentifiers().getCurie());
        return new System(config.getOwnIdentifiers().getSystem(), config.getOwnIdentifiers().getVersion());
    }

    public String getOwnCurie() {
        registerCurie(config.getOwnIdentifiers().getCurie());
        return config.getOwnIdentifiers().getCurie();
    }

    public void registerCurie(String curie) {
        try {
            CurieRegistryManager curieRegistryManager = this.curieRegistryManager.getObject();
            curieRegistryManager.registerCurie(curie);
        } catch (BeansException e) {
            log.warn("Cant register curie", e);
        }
    }
}
