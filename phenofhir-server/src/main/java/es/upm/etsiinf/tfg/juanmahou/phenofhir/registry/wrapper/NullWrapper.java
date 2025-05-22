package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import org.springframework.stereotype.Component;

@Component
public class NullWrapper implements WrapperFactory {
    /**
     * @param mapper the mapper to maybe wrap
     * @return if the wrapper should be applied to the mapper
     */
    @Override
    public <A, B> boolean shouldWrap(Mapper<A, B> mapper) {
        return !mapper.getClass().isAnnotationPresent(NoNullWrapper.class);
    }

    @Override
    public <A, B> Mapper<A, B> wrap(Mapper<A, B> mapper) {
        return new Mapper<A, B>() {
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

            @Override
            public A toPheno(B b) throws Exception {
                if (b == null) return null;
                return mapper.toPheno(b);
            }
        };
    }
}
