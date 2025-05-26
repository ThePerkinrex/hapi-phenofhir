package es.upm.etsiinf.tfg.juanmahou.phenofhir.config.constraint;

import es.upm.etsiinf.tfg.juanmahou.mapper.TypeRegistry;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsClassValidator implements ConstraintValidator<IsClass, String> {
    private final TypeRegistry typeRegistry;

    public IsClassValidator(TypeRegistry typeRegistry) {
        this.typeRegistry = typeRegistry;
    }

    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        return typeRegistry.resolve(s) != null;
    }
}
