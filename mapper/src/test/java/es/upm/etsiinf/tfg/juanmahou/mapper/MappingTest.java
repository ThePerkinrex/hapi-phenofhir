package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.field.Field;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.BaseDataResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class MappingTest {
    MapperRegistry registry;

    @Autowired
    TypeRegistry typeRegistry;

    @Autowired
    BaseDataResolver baseDataResolver;

    @BeforeEach
    void setUp() {
        registry = new MapperRegistry(List.of(), typeRegistry, baseDataResolver, List.of());
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
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(B.class),
                List.of(ResolvableType.forClass(A.class)));
        assertNotNull(runner);

        runner.run(List.of(new A()), res -> {
            assertNotNull(res);
            assertTrue(ResolvableType.forInstance(res).equalsType(ResolvableType.forClass(B.class)));
        });

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
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(B.class),
                List.of(ResolvableType.forClass(A.class)));
        assertNotNull(runner);

        A a = new A();
        a.a = 2;
        runner.run(List.of(a), res -> {
            assertNotNull(res);
            assertTrue(ResolvableType.forInstance(res).equalsType(ResolvableType.forClass(B.class)));
            B b = (B) res;
            assertEquals(a.a, b.b);
        });

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
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(B.class),
                List.of(ResolvableType.forClass(A.class)));
        assertNotNull(runner);

        A a = new A();
        a.a = 2;
        runner.run(List.of(a), res -> {
            assertNotNull(res);
            assertTrue(ResolvableType.forInstance(res).equalsType(ResolvableType.forClass(B.class)));
            B b = (B) res;
            assertEquals(b.b, a.a * 2);
        });

    }


    public static class C {
        public int id;
        public A a;
    }

    public static class D {
        public int id;
        public BWithParentId b;
    }

    public static class BWithParentId {
        public int id;
        public int b;
    }

    @Test
    void contextMappingTest() throws Exception {
        Mapping m = new Mapping();
        m.setSource(C.class.getName());
        m.setTarget(D.class.getName());
        var fId = new Field();
        fId.setSources(List.of("id"));
        fId.setId(true);
        var fCtx = new Field();
        fCtx.setSources(List.of("a"));
        m.setFields(Map.of("id", fId, "b", fCtx));


        Mapping m2 = new Mapping();
        m2.setSource(A.class.getName());
        m2.setTarget(BWithParentId.class.getName());
        var fId2 = new Field();
        fId2.setSources(List.of("ctx|resulting|" + D.class.getName() + "|field|id"));
        var fMap = new Field();
        fMap.setSources(List.of("a"));
        m2.setFields(Map.of("id", fId2, "b", fMap));

        registry.registerMapping(m);
        registry.registerMapping(m2);
        MapperRunner runner = registry.getMapper(ResolvableType.forClass(D.class),
                List.of(ResolvableType.forClass(C.class)));
        assertNotNull(runner);

        C c = new C();
        c.id = 33;
        c.a = new A();
        c.a.a = 2;
        runner.run(List.of(c), res -> {
            assertNotNull(res);
            assertTrue(ResolvableType.forInstance(res).equalsType(ResolvableType.forClass(D.class)));
            D d = (D) res;
            assertEquals(d.id, c.id);
            assertEquals(d.b.id, c.id);
        });

    }
}
