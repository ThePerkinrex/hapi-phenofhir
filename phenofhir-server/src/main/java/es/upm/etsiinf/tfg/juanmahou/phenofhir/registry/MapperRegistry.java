package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MapperRegistry {
    private record Mapping(Class<?> pheno, Class<?> fhir) {}

    Map<Mapping, Mapper<?, ?>> mappers;
    Map<String, Mapper<?, ?>> aliasedMappers;

    public MapperRegistry(List<Mapper<?, ?>> mappers) {
        this.mappers = new HashMap<>(mappers.size());
        this.aliasedMappers = new HashMap<>();
        for(Mapper<?, ?> m : mappers) {
            registerMapper(m);
        }
    }

    public void registerMapper(Mapper<?, ?> m) {
        Class<?> mapperClass = m.getClass();
        if (!mapperClass.isAnnotationPresent(MapperIgnore.class)) {
            this.mappers.put(new Mapping(m.getPhenoClass(), m.getFhirClass()), m);
        }
        MapperAlias alias = mapperClass.getAnnotation(MapperAlias.class);
        if(alias != null) {
            aliasedMappers.put(alias.value(), m);
        }
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
