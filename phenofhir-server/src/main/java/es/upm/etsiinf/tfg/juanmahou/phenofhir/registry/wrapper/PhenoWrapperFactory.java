package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;

@FunctionalInterface
public interface PhenoWrapperFactory {
    /**
     * @param mapper the mapper to maybe wrap
     * @param <A> the Pheno type of the mapper
     * @param <B> the FHIR type of the mapper
     * @return if the wrapper should be applied to the mapper
     */
    default <A, B> boolean shouldWrap(PhenoMapper<A, B> mapper) {
        return true;
    }

    <A, B> PhenoMapper<A, B> wrap(PhenoMapper<A, B> mapper);
}
