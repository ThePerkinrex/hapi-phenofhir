package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;

@FunctionalInterface
public interface FhirWrapperFactory {
    /**
     * @param mapper the mapper to maybe wrap
     * @param <A> the Pheno type of the mapper
     * @param <B> the FHIR type of the mapper
     * @return if the wrapper should be applied to the mapper
     */
    default <A, B> boolean shouldWrap(FhirMapper<A, B> mapper) {
        return true;
    }

    <A, B> FhirMapper<A, B> wrap(FhirMapper<A, B> mapper);
}
