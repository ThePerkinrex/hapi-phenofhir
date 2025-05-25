package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.KeyUtils;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.ReferenceMapperFactory;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper.FhirWrapperFactory;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper.PhenoWrapperFactory;
import org.hl7.fhir.r4b.model.IdType;
import org.hl7.fhir.r4b.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class MapperRegistry {
    private static final Logger log = LoggerFactory.getLogger(MapperRegistry.class);

    private record Mapping(String pheno, String fhir) {
    }

    private final Map<Mapping, FhirMapper<?, ?>> fhirMappers;
    private final Map<Mapping, PhenoMapper<?, ?>> phenoMappers;
    private final Map<String, FhirMapper<?, ?>> aliasedFhirMappers;
    private final Map<String, PhenoMapper<?, ?>> aliasedPhenoMappers;
    private final List<FhirWrapperFactory> fhirWrapperFactories;
    private final List<PhenoWrapperFactory> phenoWrapperFactories;
    private final KeyUtils keyUtils;

    public MapperRegistry(
            List<FhirMapper<?, ?>> fhirMappers,
            List<PhenoMapper<?, ?>> phenoMappers,
            List<FhirWrapperFactory> fhirWrapperFactories,
            List<PhenoWrapperFactory> phenoWrapperFactories,
            ReferenceMapperFactory referenceMapperFactory, KeyUtils keyUtils) {
        this.fhirMappers = new HashMap<>(fhirMappers.size());
        this.phenoMappers = new HashMap<>(phenoMappers.size());
        this.keyUtils = keyUtils;
        this.aliasedFhirMappers = new HashMap<>();
        this.aliasedPhenoMappers = new HashMap<>();
        this.fhirWrapperFactories = fhirWrapperFactories;
        this.phenoWrapperFactories = phenoWrapperFactories;
        for(FhirMapper<?, ?> m : fhirMappers) {
            registerMapper(m);
        }
        for(PhenoMapper<?, ?> m : phenoMappers) {
            registerMapper(m);
        }
        for(Mapper<?, Reference> m : referenceMapperFactory.getAll()) {
            registerMapper(m);
        }
    }

    private record CombinedMapper<A, B>(FhirMapper<A, B> fhir, PhenoMapper<A, B> pheno) implements Mapper<A, B> {

        @Override
        public B toFHIR(A a) throws Exception {
            return fhir.toFHIR(a);
        }

        @Override
        public A toPheno(B b) throws Exception {
            return pheno.toPheno(b);
        }

        @Override
        public Type getFhirClass() {
            return fhir.getFhirClass();
        }

        @Override
        public Type getPhenoClass() {
            return fhir.getPhenoClass();
        }
    }

    public <A, B> Mapper<A, B> registerMapper(Mapper<A, B> m) {
        return new CombinedMapper<>(registerMapper((FhirMapper<A, B>) m), registerMapper((PhenoMapper<A, B>) m));
    }

    public <A, B> FhirMapper<A, B> registerMapper(FhirMapper<A, B> m) {
        log.info("Registering fhir mapper {} ({} -> {})", m, m.getPhenoClass(), m.getFhirClass());
        Class<?> mapperClass = m.getClass();
        for (FhirWrapperFactory factory : fhirWrapperFactories) {
            if(factory.shouldWrap(m)) {
                log.info("Wrapping with {}", factory);
                m = factory.wrap(m);
            }
        }
        if (!mapperClass.isAnnotationPresent(MapperIgnore.class)) {
            log.info("Adding as type mapper");
            this.fhirMappers.put(new Mapping(m.getPhenoClass().getTypeName(), m.getFhirClass().getTypeName()), m);
        }
        MapperAlias alias = mapperClass.getAnnotation(MapperAlias.class);
        if(alias != null) {
            log.info("Adding aliased mapper {}", alias.value());
            aliasedFhirMappers.put(alias.value(), m);
        }
        return m;
    }

    public <A, B> PhenoMapper<A, B> registerMapper(PhenoMapper<A, B> m) {
        log.info("Registering pheno mapper {} ({} -> {})", m, m.getFhirClass(), m.getPhenoClass());
        Class<?> mapperClass = m.getClass();
        for (PhenoWrapperFactory factory : phenoWrapperFactories) {
            if(factory.shouldWrap(m)) {
                log.info("Wrapping with {}", factory);
                m = factory.wrap(m);
            }
        }
        if (!mapperClass.isAnnotationPresent(MapperIgnore.class)) {
            log.info("Adding as type mapper");
            this.phenoMappers.put(new Mapping(m.getPhenoClass().getTypeName(), m.getFhirClass().getTypeName()), m);
        }
        MapperAlias alias = mapperClass.getAnnotation(MapperAlias.class);
        if(alias != null) {
            log.info("Adding aliased mapper {}", alias.value());
            aliasedPhenoMappers.put(alias.value(), m);
        }
        return m;
    }

    public <A, B> Mapper<A, B> unregisterMapper(Mapper<A, B> m) {
        // Unregister both sides and return a CombinedMapper of the originals
        return new CombinedMapper<>(
                unregisterMapper((FhirMapper<A, B>) m),
                unregisterMapper((PhenoMapper<A, B>) m)
        );
    }

    public <A, B> FhirMapper<A, B> unregisterMapper(FhirMapper<A, B> m) {
        log.info("Unregistering fhir mapper {} ({} -> {})", m, m.getPhenoClass(), m.getFhirClass());
        Mapping key = new Mapping(m.getPhenoClass().getTypeName(), m.getFhirClass().getTypeName());
        // remove the direct lookup
        this.fhirMappers.remove(key);

        // remove any alias
        MapperAlias alias = m.getClass().getAnnotation(MapperAlias.class);
        if (alias != null) {
            log.info("Removing aliased mapper {}", alias.value());
            this.aliasedFhirMappers.remove(alias.value());
        }
        return m;
    }

    public <A, B> PhenoMapper<A, B> unregisterMapper(PhenoMapper<A, B> m) {
        log.info("Unregistering pheno mapper {} ({} -> {})", m, m.getFhirClass(), m.getPhenoClass());
        Mapping key = new Mapping(m.getPhenoClass().getTypeName(), m.getFhirClass().getTypeName());
        // remove the direct lookup
        this.phenoMappers.remove(key);

        // remove any alias
        MapperAlias alias = m.getClass().getAnnotation(MapperAlias.class);
        if (alias != null) {
            log.info("Removing aliased mapper {}", alias.value());
            this.aliasedPhenoMappers.remove(alias.value());
        }
        return m;
    }

    public <Pheno, FHIR> FhirMapper<Pheno, FHIR> getFhirMapper(Type pheno, Type fhir) throws NotFoundException {
        Mapping mapping = new Mapping(pheno.getTypeName(), fhir.getTypeName());
        if(fhirMappers.containsKey(mapping)) {
            return (FhirMapper<Pheno, FHIR>) fhirMappers.get(mapping);
        } else if (pheno.equals(fhir)) {
            return new FhirMapper<>() {
                @Override
                public FHIR toFHIR(Pheno pheno) throws Exception {
                    return (FHIR) pheno;
                }

                @Override
                public Type getFhirClass() {
                    return fhir;
                }

                @Override
                public Type getPhenoClass() {
                    return pheno;
                }
            };
        }
        throw new NotFoundException("Mapping not found for " + pheno + " -> " + fhir);
    }

    public FhirMapper<?, ?> getFhirMapper(String name) throws NotFoundException {
        if(aliasedFhirMappers.containsKey(name)) {
            return aliasedFhirMappers.get(name);
        }
        throw new NotFoundException("Mapping not found for name " + name);
    }



    public <Pheno, FHIR> PhenoMapper<Pheno, FHIR> getPhenoMapper(Type pheno, Type fhir) throws NotFoundException {
        Mapping mapping = new Mapping(pheno.getTypeName(), fhir.getTypeName());
        if(phenoMappers.containsKey(mapping)) {
            return (PhenoMapper<Pheno, FHIR>) phenoMappers.get(mapping);
        } else if (pheno.equals(fhir)) {
            return new PhenoMapper<>() {
                @Override
                public Pheno toPheno(FHIR fhir) throws Exception {
                    return (Pheno) fhir;
                }

                @Override
                public Type getFhirClass() {
                    return fhir;
                }

                @Override
                public Type getPhenoClass() {
                    return pheno;
                }
            };
        }
        throw new NotFoundException("Mapping not found for " + pheno + " <- " + fhir);
    }

    public PhenoMapper<?, ?> getPhenoMapper(String name) throws NotFoundException {
        if(aliasedPhenoMappers.containsKey(name)) {
            return aliasedPhenoMappers.get(name);
        }
        throw new NotFoundException("Mapping not found for name " + name);
    }

    public PhenoMapper<?, IdType> getKeyMapper(Type entity) throws NotFoundException {
        Type key = keyUtils.getKeyType(entity);
        return getPhenoMapper(key, IdType.class);
    }
}
