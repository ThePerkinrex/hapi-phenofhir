package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.field.Field;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.BaseDataResolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ThisResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class MappingTest {
    MapperRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new MapperRegistry(List.of(), new TypeRegistry(), new BaseDataResolver(List.of(new ThisResolver())), List.of());
    }

    public static class A {
        public int a;
    }
    public static class B {
        public int b;
    }

    @Test
    void simpleMappingTest() throws Exception {
        Mapping m = new Mapping();
        m.setSource(A.class.getName());
        m.setTarget(B.class.getName());

        registry.registerMapping(m);
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(B.class), List.of(ResolvableType.forClass(A.class)));
        assertNotNull(runner);

        Object res = runner.run(List.of(new A()));
        assertNotNull(res);
        assertTrue(ResolvableType.forInstance(res).equalsType(ResolvableType.forClass(B.class)));
    }

    @Test
    void fieldMappingTest() throws Exception {
        Mapping m = new Mapping();
        m.setSource(A.class.getName());
        m.setTarget(B.class.getName());
        var f = new Field();
        f.setSources(List.of("a"));
        m.setFields(Map.of("b", f));

        registry.registerMapping(m);
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(B.class), List.of(ResolvableType.forClass(A.class)));
        assertNotNull(runner);

        A a = new A();
        a.a = 2;
        Object res = runner.run(List.of(a));
        assertNotNull(res);
        assertTrue(ResolvableType.forInstance(res).equalsType(ResolvableType.forClass(B.class)));
        B b = (B) res;
        assertEquals(b.b, a.a);
    }

    public static class Doubler implements MapperClass {
        @Mapper
        public int doubler(Context ctx, int a) {
            return a * 2;
        }
    }

    @Test
    void fieldMappingTestOverrideDefault() throws Exception {
        Mapping m = new Mapping();
        m.setSource(A.class.getName());
        m.setTarget(B.class.getName());
        var f = new Field();
        f.setSources(List.of("a"));
        m.setFields(Map.of("b", f));

        registry.registerMappers(new Doubler());

        registry.registerMapping(m);
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(B.class), List.of(ResolvableType.forClass(A.class)));
        assertNotNull(runner);

        A a = new A();
        a.a = 2;
        Object res = runner.run(List.of(a));
        assertNotNull(res);
        assertTrue(ResolvableType.forInstance(res).equalsType(ResolvableType.forClass(B.class)));
        B b = (B) res;
        assertEquals(b.b, a.a * 2);
    }
}
