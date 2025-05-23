package es.upm.etsiinf.tfg.juanmahou.hibernate.generators.curie.uuid;

import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.generator.AnnotationBasedGenerator;
import org.hibernate.generator.BeforeExecutionGenerator;
import org.hibernate.generator.EventType;
import org.hibernate.generator.GeneratorCreationContext;

import java.lang.reflect.Member;
import java.util.EnumSet;

public class CurieUUIDv4Generator implements AnnotationBasedGenerator<CurieUUIDv4> {

    @Override
    public void initialize(CurieUUIDv4 curieUUIDv4, Member member, GeneratorCreationContext generatorCreationContext) {

    }

    @Override
    public boolean generatedOnExecution() {
        return false;
    }

    @Override
    public EnumSet<EventType> getEventTypes() {
        return null;
    }
}
