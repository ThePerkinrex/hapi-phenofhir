package es.upm.etsiinf.tfg.juanmahou.phenofhir.config.constraint;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.mapping.PhenoGenerator;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.GeneratorRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.IGenerator;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Validator;

public class IsGeneratorValidator implements ConstraintValidator<IsGenerator, PhenoGenerator> {
    private final GeneratorRegistry registry;
    private final Validator validator;

    public IsGeneratorValidator(GeneratorRegistry registry, Validator validator) {
        this.registry = registry;
        this.validator = validator;
    }

    @Override
    public boolean isValid(PhenoGenerator phenoGenerator, ConstraintValidatorContext constraintValidatorContext) {
        IGenerator<?, ?> gen = registry.get(phenoGenerator.getName());
        if(gen == null) return false;
        Object config = new ObjectMapper().convertValue(phenoGenerator.getParams(), gen.getConfigClass());
        phenoGenerator.setGenerator(new IGenerator.ConfiguredGenerator<Object, Object>((IGenerator<Object, Object>) gen, config));
        return validator.validate(config).isEmpty();
    }
}
