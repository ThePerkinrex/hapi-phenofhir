package es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.wrapper;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import jakarta.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public class Persistence implements WrapperFactory {
    private static class PersitencyWrapper<A,B> implements Mapper<A, B> {
        private static final Logger log = LoggerFactory.getLogger(PersitencyWrapper.class);
        private final Mapper<A, B> mapper;
        private final CrudRepository<A, ?> repository;

        public PersitencyWrapper(Mapper<A, B> mapper, CrudRepository<A, ?> repository) {
            this.mapper = mapper;
            this.repository = repository;
        }

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
            return mapper.toFHIR(a);
        }

        @Override
        public A toPheno(B b) throws Exception {
            A pheno = mapper.toPheno(b);
            if(pheno != null) {
                log.info("Persisting {}", pheno);
                return repository.save(pheno);
            }
            return null;
        }
    }

    private final RepositoryProvider repositoryProvider;

    public Persistence(RepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    /**
     * @param mapper the mapper to maybe wrap
     * @return if the wrapper should be applied to the mapper
     */
    @Override
    public <A, B> boolean shouldWrap(Mapper<A, B> mapper) {
        // FIXME Maybe more classes can be persisted
        return mapper.getPhenoClass().isAnnotationPresent(Entity.class);
    }

    @Override
    public <A, B> Mapper<A, B> wrap(Mapper<A, B> mapper) {
        return new PersitencyWrapper<>(mapper, repositoryProvider.getCrudRepository(mapper.getPhenoClass()));
    }
}
