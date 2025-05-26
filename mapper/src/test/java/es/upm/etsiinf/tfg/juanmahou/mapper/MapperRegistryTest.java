package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.BaseDataResolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ThisResolver;
import org.junit.jupiter.api.Test;
import org.springframework.core.ResolvableType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapperRegistryTest {
    public static class DummyMapper implements MapperClass {
        @Mapper
        public int sum(Context ctx, int a, int b) {
            return a + b;
        }
    }

    @Test
    void testSingleMapper() {
        MapperRegistry mapperRegistry = new MapperRegistry(List.of(new DummyMapper()), new TypeRegistry(),
                new BaseDataResolver(List.of(new ThisResolver())), List.of());

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
