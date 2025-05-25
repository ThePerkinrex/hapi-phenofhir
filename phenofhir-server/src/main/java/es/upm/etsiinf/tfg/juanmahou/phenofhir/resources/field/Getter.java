package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.field;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Getter<T, F> {
    private static final Logger log = LoggerFactory.getLogger(Getter.class);
    private final Function<T, F> getter;
    private final Type fieldClass;

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

    /**
     * @param clazz the class containing the field or JavaBean property
     * @param name  the property/field name
     */
    @SuppressWarnings("unchecked")
    public Getter(Class<? extends T> clazz, String name) {
        Function<T, F>   tempGetter     = null;
        Type         rawFieldClass = null;

        // 1) Try bean-style accessors
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (pd.getName().equals(name)) {
                    Method read  = pd.getReadMethod();
                    if (read  != null) read .setAccessible(true);

                    if (read != null) {
                        rawFieldClass = read.getGenericReturnType();
                        tempGetter = target -> {
                            try {
                                return (F) read.invoke(target);
                            } catch (Exception e) {
                                throw new RuntimeException("Error invoking getter for " + name, e);
                            }
                        };
                    }

                    break;
                }
            }
        } catch (IntrospectionException e) {
            // ignore
        }

        // 2) Fallback to direct field if needed
        if (tempGetter == null) {
            try {
                Field field = clazz.getField(name);
                field.setAccessible(true);
                rawFieldClass = field.getGenericType();
                tempGetter = target -> {
                    try {
                        return (F) field.get(target);
                    } catch (Exception e) {
                        throw new RuntimeException("Error reading field " + name, e);
                    }
                };

            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(
                        "Neither bean property nor field '" + name + "' found on " + clazz, e);
            }
        }

        // 3) Wrap primitive types
        Type finalFieldClass = wrapPrimitive(rawFieldClass);

        // 4) Validate
        if (finalFieldClass == null) {
            throw new IllegalArgumentException(
                    "Could not build getters/setters for '" + name + "' on " + clazz);
        }

        this.getter     = tempGetter;
        this.fieldClass = finalFieldClass;
    }

    private static Type wrapPrimitive(Type type) {
        return (type instanceof Class<?> c && c.isPrimitive()) ? PRIMITIVE_WRAPPERS.get(c) : type;
    }

    /** Read the named property/field from the given target instance. */
    public F get(T target) {
        return getter.apply(target);
    }

    /** Returns the `Class` object representing the field’s (wrapped) type. */
    public Type getFieldClass() {
        return fieldClass;
    }

    @Override
    public String toString() {
        return "Getter{" +
                "getter=" + getter +
                ", fieldClass=" + fieldClass +
                '}';
    }
}
