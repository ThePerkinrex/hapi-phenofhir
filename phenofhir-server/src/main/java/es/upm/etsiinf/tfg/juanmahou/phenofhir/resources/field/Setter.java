package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.field;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.types.TypeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class Setter<T, F> {
    private static final Logger log = LoggerFactory.getLogger(Setter.class);
    private final BiConsumer<T, F> setter;
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
    public Setter(Class<? extends T> clazz, String name) {
        BiConsumer<T, F> tempSetter    = null;
        Type         rawFieldClass = null;

        // 1) Try bean-style accessors
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (pd.getName().equals(name)) {
                    Method write = pd.getWriteMethod();
                    if (write != null) write.setAccessible(true);
                    if (write != null) {
                        Type paramType = write.getGenericParameterTypes()[0];
                        rawFieldClass = paramType;
                        tempSetter = (target, value) -> {
                            try {
                                write.invoke(target, value);
                            } catch (Exception e) {
                                throw new RuntimeException("Error invoking setter for " + name, e);
                            }
                        };
                    }
                    break;
                }
            }
        } catch (IntrospectionException e) {
            // ignore
        }

        if(tempSetter == null) {
            String setterName = "set" + Character.toUpperCase(name.charAt(0)) + name.substring(1);
            for (Method m : clazz.getMethods()) {
                if(m.getName().equals(setterName) &&
                        m.getGenericParameterTypes().length == 1 && (
                                rawFieldClass == null || TypeUtils.isAssignableFrom(rawFieldClass, m.getGenericParameterTypes()[0])
                        )) {
                    log.info("Found {}", m);
                    rawFieldClass = m.getGenericParameterTypes()[0];
                    tempSetter = (obj, value) -> {
                        try {
                            m.invoke(obj, value);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    };
                }
            }
            log.info("Finally {}: {}", setterName, rawFieldClass);
        }

        // 2) Fallback to direct field if needed
        if (tempSetter == null) {
            try {
                Field field = clazz.getField(name);
                field.setAccessible(true);
                rawFieldClass = field.getGenericType();

                tempSetter = (target, value) -> {
                    try {
                        field.set(target, value);
                    } catch (Exception e) {
                        throw new RuntimeException("Error writing field " + name, e);
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

        this.setter     = tempSetter;
        this.fieldClass = finalFieldClass;
    }

    private static Type wrapPrimitive(Type type) {
        return (type instanceof Class<?> c && c.isPrimitive()) ? PRIMITIVE_WRAPPERS.get(c) : type;
    }

    /** Write the named property/field on the given target instance. */
    public void set(T target, F value) {
        setter.accept(target, value);
    }

    /** Returns the `Class` object representing the field’s (wrapped) type. */
    public Type getFieldClass() {
        return fieldClass;
    }

    @Override
    public String toString() {
        return "Setter{" +
                "setter=" + setter +
                ", fieldClass=" + fieldClass +
                '}';
    }
}
