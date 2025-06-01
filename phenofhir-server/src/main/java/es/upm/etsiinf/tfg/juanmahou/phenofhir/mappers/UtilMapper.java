package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.hl7.fhir.r4.model.Meta;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class UtilMapper implements MapperClass {
    @Mapper
    public Instant fromDateToInstant(Context<?> ctx, Date date) {
        return date.toInstant();
    }

    @Mapper
    public Date fromInstantToDate(Context<?> ctx, Instant instant) {
        return Date.from(instant);
    }

    @Mapper
    public Meta buildMeta(Context<?> ctx, String profile) {
        return new Meta().addProfile(profile);
    }
}
