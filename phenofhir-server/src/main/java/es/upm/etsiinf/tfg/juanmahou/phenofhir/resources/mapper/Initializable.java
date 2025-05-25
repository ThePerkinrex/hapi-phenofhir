package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.mapper;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.NotFoundException;

@FunctionalInterface
public interface Initializable {
    void initialize() throws NotFoundException;
}
