package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.NotFoundException;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

public class ResourceMapping<Pheno, FHIR extends IBaseResource> implements Mapper<Pheno, FHIR> {
    private class Translation<A, B> {
        private final Logger log = LoggerFactory.getLogger(Translation.class);
        private final ResourceField<Pheno, A> phenoField;
        private final ResourceField<FHIR, B> fhirField;
        private final Mapper<A, B> mapper;

        Translation(es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation mapping) {
            phenoField = new ResourceField<>(pheno, mapping.getPhenoName());
            fhirField = new ResourceField<>(fhir, mapping.getFhirName());
            log.info("Getting mapper for {}", mapping);
            try {
                if (mapping.getMapper() != null) {
                    var mapper = registry.getMapper(mapping.getMapper());
                    if (mapper.getPhenoClass().equals(phenoField.getFieldClass()) && mapper.getFhirClass().equals(fhirField.getFieldClass())) {
                        this.mapper = (Mapper<A, B>) mapper;
                    } else {
                        throw new RuntimeException("Mapper of name " + mapping.getMapper() + " does not map the correct types");
                    }
                } else {
                    mapper = registry.getMapper(phenoField.getFieldClass(), fhirField.getFieldClass());
                }
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public void fhirToPheno(Pheno pheno, FHIR fhir) throws Exception {
            // TODO add persist
            phenoField.set(pheno, mapper.toPheno(fhirField.get(fhir)));
        }

        public void phenoToFhir(FHIR fhir, Pheno pheno) throws Exception {
            fhirField.set(fhir, mapper.toFHIR(phenoField.get(pheno)));
        }
    }

    private final Class<? extends FHIR> fhir;
    private final Class<Pheno> pheno;

    private final Constructor<? extends FHIR> fhirConstructor;
    private final Constructor<Pheno> phenoConstructor;

    private List<Translation<Object, Object>> translations;
    private final MapperRegistry registry;
    private final Mapping mapping;

    public ResourceMapping(Class<Pheno> pheno, Class<? extends FHIR> fhir, Mapping mapping, MapperRegistry registry) throws NoSuchMethodException {
        this.registry = registry;
        this.pheno = pheno;
        this.fhir = fhir;
        this.phenoConstructor = pheno.getConstructor();
        this.fhirConstructor = fhir.getConstructor();
        this.mapping = mapping;
        this.translations = List.of();
    }

    public void initialize() {
        this.translations = mapping.getTranslations().stream().map(Translation<Object, Object>::new).toList();
    }

    public Mapping getMapping() {
        return mapping;
    }

    @Override
    public Class<? extends FHIR> getFhirClass() {
        return fhir;
    }

    @Override
    public Class<Pheno> getPhenoClass() {
        return pheno;
    }

    @Override
    public Pheno toPheno(FHIR fhir) throws Exception {
        Pheno res = phenoConstructor.newInstance();
        for(Translation<?, ?> t : translations) {
            t.fhirToPheno(res, fhir);
        }
        return res;
    }

    @Override
    public FHIR toFHIR(Pheno pheno) throws Exception {
        FHIR res = fhirConstructor.newInstance();
        for(Translation<?, ?> t : translations) {
            t.phenoToFhir(res, pheno);
        }
        return res;
    }
}
