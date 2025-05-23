package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.time;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import org.hl7.fhir.r4b.model.BaseDateTimeType;
import org.hl7.fhir.r4b.model.InstantType;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Date;

@Component
public class BaseDateTimeMapper implements FhirMapper<Instant, BaseDateTimeType>, PhenoMapper<Instant, BaseDateTimeType> {
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
