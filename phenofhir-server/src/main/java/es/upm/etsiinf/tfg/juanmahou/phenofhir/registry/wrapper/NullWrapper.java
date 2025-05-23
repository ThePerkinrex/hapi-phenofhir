package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import org.springframework.stereotype.Component;

@Component
public class NullWrapper implements FhirWrapperFactory, PhenoWrapperFactory {


    /**
     * @param mapper the mapper to maybe wrap
     * @return if the wrapper should be applied to the mapper
     */
    @Override
    public <A, B> boolean shouldWrap(FhirMapper<A, B> mapper) {
        return !mapper.getClass().isAnnotationPresent(NoNullWrapper.class);
    }

    @Override
    public <A, B> boolean shouldWrap(PhenoMapper<A, B> mapper) {
        return !mapper.getClass().isAnnotationPresent(NoNullWrapper.class);
    }

    @Override
    public <A, B> FhirMapper<A, B> wrap(FhirMapper<A, B> mapper) {
        return new FhirMapper<A, B>() {
            @Override
            public Class<? extends B> getFhirClass() {
                return mapper.getFhirClass();
            }

            @Override
            public Class<A> getPhenoClass() {
                return mapper.getPhenoClass();
            }

            @Override
            public B toFHIR(A a) throws Exception {
                if (a == null) return null;
                return mapper.toFHIR(a);
            }
        };
    }

    @Override
    public <A, B> PhenoMapper<A, B> wrap(PhenoMapper<A, B> mapper) {
        return new PhenoMapper<A, B>() {
            @Override
            public Class<? extends B> getFhirClass() {
                return mapper.getFhirClass();
            }

            @Override
            public Class<A> getPhenoClass() {
                return mapper.getPhenoClass();
            }

            @Override
            public A toPheno(B b) throws Exception {
                if (b == null) return null;
                return mapper.toPheno(b);
            }
        };
    }
}
