package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.core.Individual;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.hl7.fhir.r4b.model.IdType;
import org.springframework.stereotype.Component;

@Component
public class IdMappers implements MapperClass {
    @Mapper
    public Individual.Key idAsIndividualKey(Context ctx, IdType id) {
        return new Individual.Key(id.getIdPart());
    }
}
