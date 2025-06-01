package es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence;

import es.upm.etsiinf.tfg.juanmahou.mapper.adapter.MapperResultAdapter;
import jakarta.persistence.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Component
public class PersistenceAdapter implements MapperResultAdapter {
    private static final Logger log = LoggerFactory.getLogger(PersistenceAdapter.class);
    private final RepositoryProvider repositoryProvider;

    public PersistenceAdapter(RepositoryProvider repositoryProvider) {
        this.repositoryProvider = repositoryProvider;
    }

    /**
     * @param res the object to adapt
     * @return the adapted object, of the same type
     */
    @Override
    public Object adaptOnSet(Object res) {
        return res;
    }

    /**
     * @param res the object to adapt
     * @return the adapted object, of the same type
     */
    @Override
    public Object adaptOnBuilt(Object res) {
        if(res != null && res.getClass().isAnnotationPresent(Entity.class)) {
            log.info("Persisting {}", res);
            CrudRepository<Object, Object> repository = repositoryProvider.getCrudRepository(ResolvableType.forInstance(res));
            return repository.save(res);
        }
        return res;
    }
}
