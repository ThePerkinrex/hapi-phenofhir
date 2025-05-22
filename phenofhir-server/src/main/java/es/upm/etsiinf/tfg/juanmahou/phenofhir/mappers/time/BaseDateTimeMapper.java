package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.time;

import entities.org.phenopackets.schema.v2.core.TimeElement;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import org.hl7.fhir.r4b.model.BaseDateTimeType;
import org.hl7.fhir.r4b.model.InstantType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class BaseDateTimeMapper implements Mapper<Instant, BaseDateTimeType> {
    @Override
    public Class<? extends BaseDateTimeType> getFhirClass() {
        return BaseDateTimeType.class;
    }

    @Override
    public Class<Instant> getPhenoClass() {
        return Instant.class;
    }

    @Override
    public BaseDateTimeType toFHIR(Instant timeElement) throws Exception {
        return new InstantType(Date.from(timeElement));
    }

    @Override
    public Instant toPheno(BaseDateTimeType baseDateTimeType) throws Exception {
        return baseDateTimeType.getValueAsCalendar().toInstant();
    }
}
