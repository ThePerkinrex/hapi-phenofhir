package es.upm.etsiinf.tfg.juanmahou.mapper.util;

import org.springframework.core.ResolvableType;

import java.util.HashMap;
import java.util.Map;

public class PrimitiveUtil {
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPERS = new HashMap<>();
    static {
        PRIMITIVE_WRAPPERS.put(boolean.class, Boolean.class);
        PRIMITIVE_WRAPPERS.put(byte.class,    Byte.class);
        PRIMITIVE_WRAPPERS.put(char.class,    Character.class);
        PRIMITIVE_WRAPPERS.put(double.class,  Double.class);
        PRIMITIVE_WRAPPERS.put(float.class,   Float.class);
        PRIMITIVE_WRAPPERS.put(int.class,     Integer.class);
        PRIMITIVE_WRAPPERS.put(long.class,    Long.class);
        PRIMITIVE_WRAPPERS.put(short.class,   Short.class);
        PRIMITIVE_WRAPPERS.put(void.class,    Void.class);
    }

    public static ResolvableType wrapPrimitive(ResolvableType type) {
        Class<?> c = type.getRawClass();
        return (c != null && c.isPrimitive()) ? ResolvableType.forClass(PRIMITIVE_WRAPPERS.get(c)) : type;
    }
}
