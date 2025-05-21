package es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence;

import jakarta.persistence.EntityManager;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Component;

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
    public <T, ID> SimpleJpaRepository<T, ID> getCrudRepository(Class<T> domainType) {
        // the SimpleJpaRepository constructor will inspect your @Id mapping on domainType
        return new SimpleJpaRepository<>(domainType, em);
    }
}
