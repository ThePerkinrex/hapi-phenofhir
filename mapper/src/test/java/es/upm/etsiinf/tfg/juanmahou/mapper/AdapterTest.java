package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.adapter.MapperResultAdapter;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.field.Field;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.BaseDataResolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ThisResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdapterTest {
    private static final Logger log = LoggerFactory.getLogger(AdapterTest.class);
    @Mock
    MapperResultAdapter resultAdapter;
    MapperRegistry registry;

    @BeforeEach
    void setUp() {
        when(resultAdapter.adapt(any())).thenAnswer(x -> {
            log.info("Called with {}", x);
            return x.getArgument(0);
        });
        registry = new MapperRegistry(List.of(), new TypeRegistry(), new BaseDataResolver(List.of(new ThisResolver())), List.of(resultAdapter));
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
        verify(resultAdapter, times(1)).adapt(any());
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
        verify(resultAdapter, times(2)).adapt(any());
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
        verify(resultAdapter, times(2)).adapt(any());
    }
}
