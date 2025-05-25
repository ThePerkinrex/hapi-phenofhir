package es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry;

import org.hl7.fhir.r4b.model.Resource;

public interface GeneratorContext {
    Resource getCurrentResource();

}
