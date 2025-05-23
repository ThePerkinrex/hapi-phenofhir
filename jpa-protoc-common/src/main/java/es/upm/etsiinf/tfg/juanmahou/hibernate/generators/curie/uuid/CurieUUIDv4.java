package es.upm.etsiinf.tfg.juanmahou.hibernate.generators.curie.uuid;

import es.upm.etsiinf.tfg.juanmahou.hibernate.generators.GeneratorAlias;
import org.hibernate.annotations.IdGeneratorType;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;

@GeneratorAlias("UUIDv4-CURIE")
@IdGeneratorType(CurieUUIDv4Generator.class)
@Retention(RetentionPolicy.RUNTIME)
@Target({METHOD,FIELD})
public @interface CurieUUIDv4 {
}
