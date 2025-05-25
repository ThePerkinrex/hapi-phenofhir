package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction.Resolver;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.types.TypeUtils;
import org.hl7.fhir.r4b.model.Reference;
import org.hl7.fhir.r4b.model.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.List;

@Component
public class ReferenceMapperFactory {
    public static <T> T get(Type pheno, Resolver resolver, MapperRegistry mapperRegistry, String id) throws Exception {
        Resolver.Resolved resolved = resolver.get(id);
        if(resolved.getPheno() != null) {
            if (TypeUtils.isAssignableFrom(pheno, resolved.getPheno().getClass())) {
                return (T) resolved.getPheno();
            }else{
                throw new RuntimeException("Reference was previously resolved to an unexpected type");
            }
        }
        PhenoMapper<T, Resource> phenoMapper = mapperRegistry.getPhenoMapper(pheno, resolved.getResource().getClass());
        T res = phenoMapper.toPheno(resolved.getResource());
        resolved.setPheno(res);
        return res;
    }

    private record ReferenceMapper<T>(Type pheno, ObjectProvider<Resolver> resolverProvider, ObjectProvider<MapperRegistry> mapperRegistry) implements Mapper<T, Reference> {

        @Override
        public Reference toFHIR(T t) throws Exception {
            throw new RuntimeException("Not implemented");
        }

        @Override
        public T toPheno(Reference reference) throws Exception {
            Resolver r = resolverProvider.getObject();
            String id = reference.getReference();
            MapperRegistry mapperRegistry = this.mapperRegistry().getObject();
            return get(pheno(), r, mapperRegistry, id);
        }

        @Override
        public Type getFhirClass() {
            return Reference.class;
        }

        @Override
        public Type getPhenoClass() {
            return pheno;
        }
    }

    private final ObjectProvider<Resolver> resolverProvider;
    private final ObjectProvider<MapperRegistry> mapperRegistry;
    private final Config config;


    public ReferenceMapperFactory(ObjectProvider<Resolver> resolverProvider, ObjectProvider<MapperRegistry> mapperRegistry, Config config) {
        this.resolverProvider = resolverProvider;
        this.mapperRegistry = mapperRegistry;
        this.config = config;
    }

    public List<? extends Mapper<?, Reference>> getAll() {
        return config.getMappings().values().stream().map(m -> {
            Type pheno = null;
            try {
                pheno = getClass().getClassLoader().loadClass(m.getTarget());
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
            return (Mapper<?, Reference>) new ReferenceMapper<>(pheno, resolverProvider, mapperRegistry);
        }).toList();
    }


}
