package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;

public class ResourceField<T, F> {
    private final Function<T, F> getter;
    private final BiConsumer<T, F> setter;
    private final Class<F> fieldClass;

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
    public ResourceField(Class<? extends T> clazz, String name) {
        Function<T, F>   tempGetter     = null;
        BiConsumer<T, F> tempSetter    = null;
        Class<?>         rawFieldClass = null;

        // 1) Try bean-style accessors
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (pd.getName().equals(name)) {
                    Method read  = pd.getReadMethod();
                    Method write = pd.getWriteMethod();
                    if (read  != null) read .setAccessible(true);
                    if (write != null) write.setAccessible(true);

                    if (read != null) {
                        rawFieldClass = read.getReturnType();
                        tempGetter = target -> {
                            try {
                                return (F) read.invoke(target);
                            } catch (Exception e) {
                                throw new RuntimeException("Error invoking getter for " + name, e);
                            }
                        };
                    }
                    if (write != null) {
                        Class<?> paramType = write.getParameterTypes()[0];
                        if (rawFieldClass == null) rawFieldClass = paramType;
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

        // 2) Fallback to direct field if needed
        if (tempGetter == null || tempSetter == null) {
            try {
                Field field = clazz.getDeclaredField(name);
                field.setAccessible(true);
                if (rawFieldClass == null) {
                    rawFieldClass = field.getType();
                }
                if (tempGetter == null) {
                    tempGetter = target -> {
                        try {
                            return (F) field.get(target);
                        } catch (Exception e) {
                            throw new RuntimeException("Error reading field " + name, e);
                        }
                    };
                }
                if (tempSetter == null) {
                    tempSetter = (target, value) -> {
                        try {
                            field.set(target, value);
                        } catch (Exception e) {
                            throw new RuntimeException("Error writing field " + name, e);
                        }
                    };
                }
            } catch (NoSuchFieldException e) {
                throw new IllegalArgumentException(
                        "Neither bean property nor field '" + name + "' found on " + clazz, e);
            }
        }

        // 3) Wrap primitive types
        @SuppressWarnings("unchecked")
        Class<F> finalFieldClass = (Class<F>) wrapPrimitive(rawFieldClass);

        // 4) Validate
        if (finalFieldClass == null) {
            throw new IllegalArgumentException(
                    "Could not build getters/setters for '" + name + "' on " + clazz);
        }

        this.getter     = tempGetter;
        this.setter     = tempSetter;
        this.fieldClass = finalFieldClass;
    }

    private static Class<?> wrapPrimitive(Class<?> type) {
        return type.isPrimitive() ? PRIMITIVE_WRAPPERS.get(type) : type;
    }

    /** Read the named property/field from the given target instance. */
    public F get(T target) {
        return getter.apply(target);
    }

    /** Write the named property/field on the given target instance. */
    public void set(T target, F value) {
        setter.accept(target, value);
    }

    /** Returns the `Class` object representing the field’s (wrapped) type. */
    public Class<F> getFieldClass() {
        return fieldClass;
    }
}
