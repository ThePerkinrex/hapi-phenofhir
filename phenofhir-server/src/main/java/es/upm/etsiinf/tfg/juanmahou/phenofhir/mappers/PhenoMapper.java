package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

public interface PhenoMapper<Pheno, FHIR> extends BaseMapper  {
    Pheno toPheno(FHIR fhir) throws Exception; // TODO Custom exception
}
