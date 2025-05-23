package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

public interface FhirMapper<Pheno, FHIR> extends BaseMapper<Pheno, FHIR> {
    FHIR toFHIR(Pheno pheno) throws Exception; // TODO Custom exception
}
