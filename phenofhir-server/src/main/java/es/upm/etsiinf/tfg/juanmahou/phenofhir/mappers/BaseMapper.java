package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

public interface BaseMapper<Pheno, FHIR> {
    Class<? extends FHIR> getFhirClass();
    Class<Pheno> getPhenoClass();
}
