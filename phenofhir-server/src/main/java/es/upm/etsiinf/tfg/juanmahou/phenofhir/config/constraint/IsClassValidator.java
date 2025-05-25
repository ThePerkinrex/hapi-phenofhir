package es.upm.etsiinf.tfg.juanmahou.phenofhir.config.constraint;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class IsClassValidator implements ConstraintValidator<IsClass, String> {
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        try {
            getClass().getClassLoader().loadClass(s);
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
