package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper.FhirWrapperFactory;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper.PhenoWrapperFactory;
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

    private final Map<Mapping, FhirMapper<?, ?>> fhirMappers;
    private final Map<Mapping, PhenoMapper<?, ?>> phenoMappers;
    private final Map<String, FhirMapper<?, ?>> aliasedFhirMappers;
    private final Map<String, PhenoMapper<?, ?>> aliasedPhenoMappers;
    private final List<FhirWrapperFactory> fhirWrapperFactories;
    private final List<PhenoWrapperFactory> phenoWrapperFactories;

    public MapperRegistry(List<FhirMapper<?, ?>> fhirMappers, List<PhenoMapper<?, ?>> phenoMappers, List<FhirWrapperFactory> fhirWrapperFactories, List<PhenoWrapperFactory> phenoWrapperFactories) {
        this.fhirMappers = new HashMap<>(fhirMappers.size());
        this.phenoMappers = new HashMap<>(phenoMappers.size());
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
        public Class<? extends B> getFhirClass() {
            return fhir.getFhirClass();
        }

        @Override
        public Class<A> getPhenoClass() {
            return fhir.getPhenoClass();
        }
    }

    public <A, B> Mapper<A, B> registerMapper(Mapper<A, B> m) {
        return new CombinedMapper<>(registerMapper((FhirMapper<A, B>) m), registerMapper((PhenoMapper<A, B>) m));
    }

    public <A, B> FhirMapper<A, B> registerMapper(FhirMapper<A, B> m) {
        log.info("Registering mapper {}", m);
        Class<?> mapperClass = m.getClass();
        for (FhirWrapperFactory factory : fhirWrapperFactories) {
            if(factory.shouldWrap(m)) {
                log.info("Wrapping with {}", factory);
                m = factory.wrap(m);
            }
        }
        if (!mapperClass.isAnnotationPresent(MapperIgnore.class)) {
            log.info("Adding as type mapper");
            this.fhirMappers.put(new Mapping(m.getPhenoClass(), m.getFhirClass()), m);
        }
        MapperAlias alias = mapperClass.getAnnotation(MapperAlias.class);
        if(alias != null) {
            log.info("Adding aliased mapper {}", alias.value());
            aliasedFhirMappers.put(alias.value(), m);
        }
        return m;
    }

    public <A, B> PhenoMapper<A, B> registerMapper(PhenoMapper<A, B> m) {
        log.info("Registering mapper {}", m);
        Class<?> mapperClass = m.getClass();
        for (PhenoWrapperFactory factory : phenoWrapperFactories) {
            if(factory.shouldWrap(m)) {
                log.info("Wrapping with {}", factory);
                m = factory.wrap(m);
            }
        }
        if (!mapperClass.isAnnotationPresent(MapperIgnore.class)) {
            log.info("Adding as type mapper");
            this.phenoMappers.put(new Mapping(m.getPhenoClass(), m.getFhirClass()), m);
        }
        MapperAlias alias = mapperClass.getAnnotation(MapperAlias.class);
        if(alias != null) {
            log.info("Adding aliased mapper {}", alias.value());
            aliasedPhenoMappers.put(alias.value(), m);
        }
        return m;
    }

    public <Pheno, FHIR> FhirMapper<Pheno, FHIR> getFhirMapper(Class<Pheno> pheno, Class<FHIR> fhir) throws NotFoundException {
        Mapping mapping = new Mapping(pheno, fhir);
        if(fhirMappers.containsKey(mapping)) {
            return (FhirMapper<Pheno, FHIR>) fhirMappers.get(mapping);
        }
        throw new NotFoundException("Mapping not found for " + pheno + " <-> " + fhir);
    }

    public FhirMapper<?, ?> getFhirMapper(String name) throws NotFoundException {
        if(aliasedFhirMappers.containsKey(name)) {
            return aliasedFhirMappers.get(name);
        }
        throw new NotFoundException("Mapping not found for name " + name);
    }



    public <Pheno, FHIR> PhenoMapper<Pheno, FHIR> getPhenoMapper(Class<Pheno> pheno, Class<FHIR> fhir) throws NotFoundException {
        Mapping mapping = new Mapping(pheno, fhir);
        if(phenoMappers.containsKey(mapping)) {
            return (PhenoMapper<Pheno, FHIR>) phenoMappers.get(mapping);
        }
        throw new NotFoundException("Mapping not found for " + pheno + " <-> " + fhir);
    }

    public PhenoMapper<?, ?> getPhenoMapper(String name) throws NotFoundException {
        if(aliasedPhenoMappers.containsKey(name)) {
            return aliasedPhenoMappers.get(name);
        }
        throw new NotFoundException("Mapping not found for name " + name);
    }
}
