package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.mapper;

import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.NotFoundException;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.field.Getter;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.field.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ToFhirConfiguredMapping<Pheno extends WithId<? extends Id>, FHIR extends IBaseResource> implements FhirMapper<Pheno, FHIR>, Initializable {
    private class Translation<A, B> {
        private final Logger log = LoggerFactory.getLogger(Translation.class);
        private final Getter<Pheno, A> phenoField;
        private final Setter<FHIR, B> fhirField;
        private final FhirMapper<A, B> fhirMapper;

        Translation(es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation mapping) throws NotFoundException {
            phenoField = new Getter<>(pheno, mapping.getPhenoName());
            fhirField = new Setter<>(fhir, mapping.getFhirName());
            log.info("Getting mapper for {}", mapping);
            if (mapping.getMapper() != null) {
                var fhirMapper = registry.getFhirMapper(mapping.getMapper());
                if (fhirMapper.getPhenoClass().equals(phenoField.getFieldClass()) && fhirMapper.getFhirClass().equals(fhirField.getFieldClass())) {
                    this.fhirMapper = (FhirMapper<A, B>) fhirMapper;
                } else {
                    throw new NotFoundException("Mapper of name " + mapping.getMapper() + " does not map the correct types");
                }
            } else {
                fhirMapper = registry.getFhirMapper(phenoField.getFieldClass(), fhirField.getFieldClass());
            }
        }
        public void phenoToFhir(FHIR fhir, Pheno pheno) throws Exception {
            fhirField.set(fhir, fhirMapper.toFHIR(phenoField.get(pheno)));
        }
    }

    private final Class<? extends FHIR> fhir;
    private final Class<Pheno> pheno;

    private final Constructor<? extends FHIR> fhirConstructor;

    private List<Translation<Object, Object>> translations;
    private final MapperRegistry registry;
    private final Mapping mapping;

    public ToFhirConfiguredMapping(Class<Pheno> pheno, Class<? extends FHIR> fhir, Mapping mapping, MapperRegistry registry) throws NoSuchMethodException {
        this.registry = registry;
        this.pheno = pheno;
        this.fhir = fhir;
        this.fhirConstructor = fhir.getConstructor();
        this.mapping = mapping;
        this.translations = List.of();
    }

    @Override
    public void initialize() throws NotFoundException {
        List<Translation<Object, Object>> list = new ArrayList<>();
        for (es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation translation : mapping.getTranslations()) {
            Translation<Object, Object> objectObjectTranslation = new Translation<>(translation);
            list.add(objectObjectTranslation);
        }
        this.translations = list;
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
    public FHIR toFHIR(Pheno pheno) throws Exception {
        FHIR res = fhirConstructor.newInstance();
        res.getMeta().addProfile(mapping.getProfile());
        for(Translation<?, ?> t : translations) {
            t.phenoToFhir(res, pheno);
        }
        return res;
    }
}
