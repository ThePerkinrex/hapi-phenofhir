package es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence;

import org.springframework.data.repository.Repository;

import java.util.List;

public interface CurieMappingRepository extends Repository<CurieMapping, Long> {
    List<CurieMapping> findByCurie(String curie);
    CurieMapping findBySystem(String system);
    CurieMapping findBySystemAndVersion(String system, String version);
}
