package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.ontology;

import entities.org.phenopackets.schema.v2.core.OntologyClass;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Curie;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.CurieMapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.CurieMappingRepository;
import org.hl7.fhir.r4b.model.Coding;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class CodingMapper implements FhirMapper<OntologyClass, Coding>, PhenoMapper<OntologyClass, Coding> {

    private Config config;
    private CurieMappingRepository repository;
    private Map<String, CurieMapping> curieCache = new ConcurrentHashMap<>();
    private Map<Curie.System, CurieMapping> systemCache = new ConcurrentHashMap<>();

    public CodingMapper(Config config, CurieMappingRepository repository) {
        this.config = config;
        this.repository = repository;
    }

    private CurieMapping getMappingForCurie(String curie) {
        return curieCache.computeIfAbsent(curie, k -> {
            List<Curie.System> systems = config.getCurie().getMapping().getOrDefault(curie, List.of());
            if (!systems.isEmpty()) {
                Curie.System sys = systems.getFirst();
                return new CurieMapping(k, sys.getSystem(), sys.getVersion());
            }
            List<CurieMapping> mappings = repository.findByCurie(curie);
            if (!mappings.isEmpty()) {
                return mappings.getFirst();
            }
            return null;
        });
    }

    private CurieMapping getMappingForSystem(Curie.System system) {
        return systemCache.computeIfAbsent(system, k -> {
            for (Map.Entry<String, List<Curie.System>> e : config.getCurie().getMapping().entrySet()) {
                boolean matchesSystem = false;
                for (Curie.System s : e.getValue()) { // TODO usar un set en vez de una lista?
                    if (s.equals(system)) {
                        return new CurieMapping(e.getKey(), system.getSystem(), system.getVersion());
                    } else if (s.getSystem().equals(system.getSystem())) {
                        matchesSystem = true;
                    }
                }
                if (matchesSystem) {
                    // TODO Generate a new curie for system + version
                    throw new RuntimeException("Unimplemented");
                }
            }
            CurieMapping mapping = repository.findBySystemAndVersion(system.getSystem(), system.getVersion());
            if (mapping != null) {
                return mapping;
            }
            // TODO Create a mapping
            throw new RuntimeException("Unimplemented: No defined curie mapping for " + system);
        });
    }

    @Override
    public Class<Coding> getFhirClass() {
        return Coding.class;
    }

    @Override
    public Class<OntologyClass> getPhenoClass() {
        return OntologyClass.class;
    }

    @Override
    public Coding toFHIR(OntologyClass ontologyClass) throws Exception {
        // TODO
        return null;
    }

    @Override
    public OntologyClass toPheno(Coding coding) throws Exception {
        Curie.System system = new Curie.System(coding.getSystem(), coding.getVersion());
        CurieMapping mapping = getMappingForSystem(system);
        return new OntologyClass()
                .setId(new OntologyClass.Key(mapping.getCurie() + ":" + coding.getCode()))
                .setLabel(coding.getDisplay());
    }
}
