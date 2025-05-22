package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper.WrapperFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MapperRegistry {
    private static final Logger log = LoggerFactory.getLogger(MapperRegistry.class);

    private record Mapping(Class<?> pheno, Class<?> fhir) {}

    private final Map<Mapping, Mapper<?, ?>> mappers;
    private final Map<String, Mapper<?, ?>> aliasedMappers;
    private final List<WrapperFactory> wrapperFactories;

    public MapperRegistry(List<Mapper<?, ?>> mappers, List<WrapperFactory> wrapperFactories) {
        this.mappers = new HashMap<>(mappers.size());
        this.aliasedMappers = new HashMap<>();
        this.wrapperFactories = wrapperFactories;
        for(Mapper<?, ?> m : mappers) {
            registerMapper(m);
        }
    }

    public <A, B> Mapper<A, B> registerMapper(Mapper<A, B> m) {
        log.info("Registering mapper {}", m);
        Class<?> mapperClass = m.getClass();
        for (WrapperFactory factory : wrapperFactories) {
            if(factory.shouldWrap(m)) {
                log.info("Wrapping with {}", factory);
                m = factory.wrap(m);
            }
        }
        if (!mapperClass.isAnnotationPresent(MapperIgnore.class)) {
            log.info("Adding as type mapper");
            this.mappers.put(new Mapping(m.getPhenoClass(), m.getFhirClass()), m);
        }
        MapperAlias alias = mapperClass.getAnnotation(MapperAlias.class);
        if(alias != null) {
            log.info("Adding aliased mapper {}", alias.value());
            aliasedMappers.put(alias.value(), m);
        }
        return m;
    }

    public <Pheno, FHIR> Mapper<Pheno, FHIR> getMapper(Class<Pheno> pheno, Class<FHIR> fhir) throws NotFoundException {
        Mapping mapping = new Mapping(pheno, fhir);
        if(mappers.containsKey(mapping)) {
            return (Mapper<Pheno, FHIR>) mappers.get(mapping);
        }
        throw new NotFoundException("Mapping not found for " + pheno + " <-> " + fhir);
    }

    public Mapper<?, ?> getMapper(String name) throws NotFoundException {
        if(aliasedMappers.containsKey(name)) {
            return aliasedMappers.get(name);
        }
        throw new NotFoundException("Mapping not found for name " + name);
    }
}
