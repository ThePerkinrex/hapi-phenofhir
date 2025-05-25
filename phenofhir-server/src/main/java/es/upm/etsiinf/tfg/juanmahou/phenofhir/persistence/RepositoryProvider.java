package es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.types.TypeUtils;
import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;

@Component
public class RepositoryProvider {
    private final EntityManager em;

    public RepositoryProvider(EntityManager em) {
        this.em = em;
    }

    /**
     * @param domainType the entity class
     * @param <T>        entity type
     * @param <ID>       id type
     * @return a CrudRepository<T,ID> instance backed by JPA
     */
    public <T, ID> SimpleJpaRepository<T, ID> getCrudRepository(Type domainType) {
        // the SimpleJpaRepository constructor will inspect your @Id mapping on domainType
        return new SimpleJpaRepository<>((Class<T>) TypeUtils.toClass(domainType), em);
    }
}
