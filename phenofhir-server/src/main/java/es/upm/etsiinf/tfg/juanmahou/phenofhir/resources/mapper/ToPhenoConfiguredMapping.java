package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.mapper;

import ca.uhn.fhir.rest.annotation.Initialize;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.GeneratorContext;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.IGenerator;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.NotFoundException;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.field.Getter;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.field.Setter;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

public class ToPhenoConfiguredMapping<Pheno extends WithId<? extends Id>, FHIR extends IBaseResource> implements PhenoMapper<Pheno, FHIR>, Initializable {


    private class Translation<A, B> {
        private final Logger log = LoggerFactory.getLogger(Translation.class);
        private final Setter<Pheno, A> phenoField;
        private final Getter<FHIR, B> fhirField;
        private final PhenoMapper<A, B> mapper;

        Translation(es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation mapping) throws NotFoundException {
            phenoField = new Setter<>(pheno, mapping.getPhenoName());
            fhirField = new Getter<>(fhir, mapping.getFhirName());
            log.info("Getting mapper for {}", mapping);
            if (mapping.getMapper() != null) {
                var mapper = registry.getPhenoMapper(mapping.getMapper());
                if (mapper.getPhenoClass().equals(phenoField.getFieldClass()) && mapper.getFhirClass().equals(fhirField.getFieldClass())) {
                    this.mapper = (PhenoMapper<A, B>) mapper;
                } else {
                    throw new NotFoundException("Mapper of name " + mapping.getMapper() + " does not map the correct types");
                }
            } else {
                mapper = registry.getPhenoMapper(phenoField.getFieldClass(), fhirField.getFieldClass());
            }


        }

        public void fhirToPheno(Pheno pheno, FHIR fhir) throws Exception {
            phenoField.set(pheno, mapper.toPheno(fhirField.get(fhir)));
        }
    }

    private class PhenoField<A, B> {
        private final Setter<Pheno, A> phenoField;
        private final IGenerator.ConfiguredGenerator<B, ?> generator;
        private final PhenoMapper<A, B> mapper;

        public PhenoField(es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.PhenoField field) {
            this.phenoField = new Setter<>(pheno, field.getName());
            this.generator = (IGenerator.ConfiguredGenerator<B, ?>) field.getGenerator().getGenerator();
            try {
                this.mapper = registry.getPhenoMapper(phenoField.getFieldClass(), generator.getTargetClass());
            } catch (NotFoundException e) {
                throw new RuntimeException(e);
            }

        }

        public void assign(Pheno pheno) throws Exception {
            phenoField.set(pheno, mapper.toPheno(generator.generate(contextObjectProvider.getObject())));
        }
    }

    private final Class<? extends FHIR> fhir;
    private final Class<Pheno> pheno;

    private final Constructor<Pheno> phenoConstructor;

    private List<Translation<Object, Object>> translations;
    private List<PhenoField<Object, Object>> phenoFields;

    private final MapperRegistry registry;
    private final Mapping mapping;
    private final ObjectProvider<GeneratorContext> contextObjectProvider;


    public ToPhenoConfiguredMapping(Class<Pheno> pheno, Class<? extends FHIR> fhir, Mapping mapping, MapperRegistry registry, ObjectProvider<GeneratorContext> contextObjectProvider) throws NoSuchMethodException {
        this.registry = registry;
        this.pheno = pheno;
        this.fhir = fhir;
        this.phenoConstructor = pheno.getConstructor();
        this.mapping = mapping;
        this.contextObjectProvider = contextObjectProvider;
        this.translations = List.of();
        this.phenoFields = List.of();
    }

    @Override
    public void initialize() throws NotFoundException {
        List<Translation<Object, Object>> list = new ArrayList<>();
        for (es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.Translation translation : mapping.getTranslations()) {
            Translation<Object, Object> objectObjectTranslation = new Translation<>(translation);
            list.add(objectObjectTranslation);
        }
        this.translations = list;
        this.phenoFields = mapping.getPhenoFields().stream().map(PhenoField<Object, Object>::new).toList();
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

        for(PhenoField<?, ?> phenoField : phenoFields) {
            phenoField.assign(res);
        }
        return res;
    }
}
