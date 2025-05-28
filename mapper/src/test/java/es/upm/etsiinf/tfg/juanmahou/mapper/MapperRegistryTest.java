package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.BaseDataResolver;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfig.class)
public class MapperRegistryTest {
    public static class DummyMapper implements MapperClass {
        @Mapper
        public int sum(Context ctx, int a, int b) {
            return a + b;
        }
    }


    @Autowired
    TypeRegistry typeRegistry;

    @Autowired
    BaseDataResolver baseDataResolver;

    @Test
    void testSingleMapper() {
        MapperRegistry mapperRegistry = new MapperRegistry(List.of(new DummyMapper()), typeRegistry,
                baseDataResolver, List.of());

        assertNotNull(mapperRegistry.getMapper(ResolvableType.forClass(Integer.class),
                List.of(ResolvableType.forClass(Integer.class), ResolvableType.forClass(Integer.class))));

        assertNull(mapperRegistry.getMapper(ResolvableType.forClass(Long.class),
                List.of(ResolvableType.forClass(Integer.class), ResolvableType.forClass(Integer.class))));
        assertNull(mapperRegistry.getMapper(ResolvableType.forClass(Integer.class),
                List.of(ResolvableType.forClass(Integer.class), ResolvableType.forClass(Integer.class)), "other"));
    }

    @Test
    void testKeyEquals() {
        var intType = ResolvableType.forClass(int.class);
        assertEquals(new MapperRegistry.MapperKey(intType, List.of()), new MapperRegistry.MapperKey(intType,
                List.of()));
        assertEquals(new MapperRegistry.MapperKey(intType, List.of()).hashCode(),
                new MapperRegistry.MapperKey(intType, List.of()).hashCode());
        assertEquals(new MapperRegistry.MapperKey(ResolvableType.forClass(long.class),
                        List.of(ResolvableType.forClass(int.class), ResolvableType.forClass(int.class))),
                new MapperRegistry.MapperKey(ResolvableType.forClass(long.class),
                        List.of(ResolvableType.forClass(int.class), ResolvableType.forClass(int.class))));
    }
}
