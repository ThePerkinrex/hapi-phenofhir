package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import java.lang.reflect.Type;

public interface BaseMapper {
    Type getFhirClass();
    Type getPhenoClass();
}
