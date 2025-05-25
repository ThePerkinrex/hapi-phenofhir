package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.time;

import entities.org.phenopackets.schema.v2.core.TimeElement;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import org.hl7.fhir.r4b.model.DataType;
import org.springframework.stereotype.Component;

@Component
public class DataTypeMapper implements FhirMapper<TimeElement, DataType>, PhenoMapper<TimeElement, DataType> {
    private final BaseDateTimeMapper baseDateTimeMapper;

    public DataTypeMapper(BaseDateTimeMapper baseDateTimeMapper) {
        this.baseDateTimeMapper = baseDateTimeMapper;
    }

    @Override
    public Class<DataType> getFhirClass() {
        return DataType.class;
    }

    @Override
    public Class<TimeElement> getPhenoClass() {
        return TimeElement.class;
    }

    @Override
    public DataType toFHIR(TimeElement timeElement) throws Exception {
        throw new RuntimeException("Not implemented"); // TODO
    }

    @Override
    public TimeElement toPheno(DataType dataType) throws Exception {
        if(dataType.isDateTime()) {
            TimeElement te = new TimeElement();
            te.setTimestamp(baseDateTimeMapper.toPheno(dataType.dateTimeValue()));
            return te;
        }
        throw new RuntimeException("Not implemented " + dataType.fhirType()); // TODO
    }
}
