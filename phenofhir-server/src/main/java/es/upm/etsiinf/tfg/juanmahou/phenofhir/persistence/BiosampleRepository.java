package es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence;

import entities.org.phenopackets.schema.v2.core.Biosample;
import org.springframework.data.repository.CrudRepository;

public interface BiosampleRepository extends CrudRepository<Biosample, Biosample.Key> {
}
