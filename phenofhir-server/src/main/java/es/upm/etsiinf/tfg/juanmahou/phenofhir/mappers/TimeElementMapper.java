package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.core.AgeRange;
import entities.org.phenopackets.schema.v2.core.TimeElement;
import entities.org.phenopackets.schema.v2.core.TimeInterval;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRunner;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.hl7.fhir.r4b.model.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.BiConsumer;

@Component
public class TimeElementMapper implements MapperClass {
    private final ObjectProvider<MapperRegistry> mapperRegistry;

    public TimeElementMapper(ObjectProvider<MapperRegistry> mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    @Mapper
    public void timeElementMapper(Context<TimeElement> ctx, DataType dataType) throws Exception {
        MapperRegistry registry = mapperRegistry.getObject();

        if (dataType == null) {
            ctx.accept(null);
            return;
        }

        // --- dateTime branch ---
        if (dataType.isDateTime()) {
            MapperRunner dateTimeRunner = registry.getMapper(ResolvableType.forClass(Instant.class),
                    List.of(ResolvableType.forClass(BaseDateTimeType.class)));
            dateTimeRunner.run(ctx, List.of(dataType.dateTimeValue()), before -> { /* no-op */ },
                    instant -> ctx.accept(new TimeElement().setTimestamp((Instant) instant)));
            return;
        }

        // --- Age branch ---
        if (dataType instanceof Age fhirAge) {
            MapperRunner ageRunner =
                    registry.getMapper(ResolvableType.forClass(entities.org.phenopackets.schema.v2.core.Age.class),
                            List.of(ResolvableType.forClass(Age.class)));
            ageRunner.run(ctx, List.of(fhirAge), before -> { /* no-op */ },
                    phenopacketAge -> ctx.accept(new TimeElement().setAge((entities.org.phenopackets.schema.v2.core.Age) phenopacketAge)));
            return;
        }

        // --- Range > AgeRange branch ---
        if (dataType instanceof Range fhirRange) {
            MapperRunner rangeRunner = registry.getMapper(ResolvableType.forClass(AgeRange.class),
                    List.of(ResolvableType.forClass(Range.class)));
            rangeRunner.run(ctx, List.of(fhirRange), before -> { /* no-op */ },
                    phenopacketAgeRange -> ctx.accept(new TimeElement().setAge_range((AgeRange) phenopacketAgeRange)));
            return;
        }

        // --- Period > TimeInterval branch ---
        if (dataType instanceof Period fhirPeriod) {
            MapperRunner periodRunner = registry.getMapper(ResolvableType.forClass(TimeInterval.class),
                    List.of(ResolvableType.forClass(Period.class)));
            periodRunner.run(ctx, List.of(fhirPeriod), before -> { /* no-op */ },
                    phenopacketInterval -> ctx.accept(new TimeElement().setInterval((TimeInterval) phenopacketInterval)));
            return;
        }

        // --- nothing matched ---
        throw new RuntimeException("Unknown timeElement DataType: " + dataType.getClass().getSimpleName());
    }

    @Mapper
    public Instant instantMapper(Context<?> ctx, BaseDateTimeType dateTimeType) {
        return dateTimeType.getValue().toInstant();
    }

    @Mapper
    public entities.org.phenopackets.schema.v2.core.Age ageMapper(Context<?> ctx, Age age) {
        // 1. Validate presence of value and code
        if (age.getValue() == null || age.getCode() == null) {
            throw new RuntimeException("FHIR Age must have both value and UCUM code");
        }
        // 2. Validate system if present (must be UCUM)
        String system = age.getSystem();
        if (system != null && !"http://unitsofmeasure.org".equals(system)) {
            throw new RuntimeException("Unsupported Age system: " + system);
        }
        // 3. Normalize numeric value
        BigDecimal value = age.getValue().stripTrailingZeros();
        String code = age.getCode();
        String isoDuration;
        // 4. Map UCUM code to ISO 8601 duration designator
        switch (code) {
            case "a":  // years
            case "yr":
                isoDuration = "P" + value.toPlainString() + "Y";
                break;
            case "mo": // months
                isoDuration = "P" + value.toPlainString() + "M";
                break;
            case "wk": // weeks
                isoDuration = "P" + value.toPlainString() + "W";
                break;
            case "d":  // days
                isoDuration = "P" + value.toPlainString() + "D";
                break;
            default:
                throw new RuntimeException("Unsupported Age unit: " + code);
        }
        // 5. Build and return the Phenopackets v2 Age
        return new entities.org.phenopackets.schema.v2.core.Age().setIso8601duration(isoDuration);
    }

    @Mapper
    public void mapAgeRangeFromRange(Context<AgeRange> ctx, Range ageRange) throws Exception {
        MapperRegistry registry = mapperRegistry.getObject();
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(AgeRange.class), List.of(ResolvableType.forClass(Age.class), ResolvableType.forClass(Age.class)));
        if (ageRange.getLow() instanceof Age low && ageRange.getHigh() instanceof Age high) {
            runner.run(ctx, List.of(low, high), x -> {}, range -> ctx.accept((AgeRange) range));
        } else {
            throw new RuntimeException("Range composed of elements not of type Age");
        }
    }

    private static class AgeRangeCollector {
        private entities.org.phenopackets.schema.v2.core.Age start = null;
        private entities.org.phenopackets.schema.v2.core.Age end = null;

        private final BiConsumer<entities.org.phenopackets.schema.v2.core.Age, entities.org.phenopackets.schema.v2.core.Age> consumer;

        public AgeRangeCollector(BiConsumer<entities.org.phenopackets.schema.v2.core.Age, entities.org.phenopackets.schema.v2.core.Age> consumer) {
            this.consumer = consumer;
        }

        public void setStart(entities.org.phenopackets.schema.v2.core.Age start) {
            this.start = start;
            check();
        }

        public void setEnd(entities.org.phenopackets.schema.v2.core.Age end) {
            this.end = end;
            check();
        }

        private void check() {
            if(this.start != null && this.end != null) {
                consumer.accept(this.start, this.end);
            }
        }
    }

    @Mapper
    public void mapAgeRangeFromAges(Context<AgeRange> ctx, Age low, Age high) throws Exception {
        MapperRegistry registry = mapperRegistry.getObject();
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(entities.org.phenopackets.schema.v2.core.Age.class), List.of(ResolvableType.forClass(Age.class)));
        AgeRangeCollector collector = new AgeRangeCollector((start, end) -> ctx.accept(new AgeRange().setStart(start).setEnd(end)));
        runner.run(ctx, List.of(low), x -> {}, start -> collector.setStart((entities.org.phenopackets.schema.v2.core.Age) start));
        runner.run(ctx, List.of(high), x -> {}, end -> collector.setEnd((entities.org.phenopackets.schema.v2.core.Age) end));
    }

    @Mapper
    public TimeInterval mapInterval(Context<?> ctx, Period period) {
        return new TimeInterval().setStart(instantMapper(ctx, period.getStartElement())).setEnd(instantMapper(ctx,
                period.getEndElement()));
    }
}
