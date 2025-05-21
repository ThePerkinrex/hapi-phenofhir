package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.core.TimeElement;
import org.hl7.fhir.r4b.model.DataType;
import org.springframework.stereotype.Component;

@Component
public class TimeElementMapper implements Mapper<TimeElement, DataType>{

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

        throw new RuntimeException("Not implemented"); // TODO
    }
}
