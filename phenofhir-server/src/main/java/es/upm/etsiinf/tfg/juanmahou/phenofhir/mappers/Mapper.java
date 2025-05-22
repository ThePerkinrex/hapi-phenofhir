package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

public interface Mapper<Pheno, FHIR> {
    Class<? extends FHIR> getFhirClass();
    Class<Pheno> getPhenoClass();

    FHIR toFHIR(Pheno pheno) throws Exception; // TODO Custom exception
    Pheno toPheno(FHIR fhir) throws Exception; // TODO Custom exception
}
