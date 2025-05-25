import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.core.ResolvableType;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MapperRegistryTest {
    public static class DummyMapper implements MapperClass {
        @Mapper
        public int sum(int a, int b) {
            return a + b;
        }
    }

    @Test
    void testSingleMapper() {
        MapperRegistry mapperRegistry = new MapperRegistry(List.of(new DummyMapper()));

        assertNotNull(mapperRegistry.getMapper(ResolvableType.forClass(int.class), List.of(ResolvableType.forClass(int.class), ResolvableType.forClass(int.class))));

        assertNull(mapperRegistry.getMapper(ResolvableType.forClass(long.class), List.of(ResolvableType.forClass(int.class), ResolvableType.forClass(int.class))));
        assertNull(mapperRegistry.getMapper(ResolvableType.forClass(int.class), List.of(ResolvableType.forClass(int.class), ResolvableType.forClass(int.class)), "other"));
    }

    @Test
    void testKeyEquals() {
        var intType = ResolvableType.forClass(int.class);
        assertEquals(new MapperRegistry.MapperKey(intType, List.of()), new MapperRegistry.MapperKey(intType, List.of()));
        assertEquals(new MapperRegistry.MapperKey(intType, List.of()).hashCode(), new MapperRegistry.MapperKey(intType, List.of()).hashCode());
        assertEquals(new MapperRegistry.MapperKey(ResolvableType.forClass(long.class), List.of(ResolvableType.forClass(int.class), ResolvableType.forClass(int.class))),
                new MapperRegistry.MapperKey(ResolvableType.forClass(long.class), List.of(ResolvableType.forClass(int.class), ResolvableType.forClass(int.class))));
    }
}
