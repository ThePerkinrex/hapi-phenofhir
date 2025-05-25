package es.upm.etsiinf.tfg.juanmahou.phenofhir.config.constraint;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy=IsClassValidator.class)
public @interface IsClass {
    String message() default "must be a valid class name";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
