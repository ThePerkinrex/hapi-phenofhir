package es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.entities.CurieMapping;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface CurieMappingRepository extends CrudRepository<CurieMapping, Long> {
    List<CurieMapping> findByCurie(String curie);
    CurieMapping findBySystem(String system);
    CurieMapping findBySystemAndVersion(String system, String version);
}
