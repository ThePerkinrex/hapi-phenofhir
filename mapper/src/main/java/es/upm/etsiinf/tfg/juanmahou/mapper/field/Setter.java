package es.upm.etsiinf.tfg.juanmahou.mapper.field;

import es.upm.etsiinf.tfg.juanmahou.mapper.util.PrimitiveUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.BiConsumer;

public class Setter<T, F> {
    private static final Logger log = LoggerFactory.getLogger(Setter.class);
    private final BiConsumer<T, F> setter;
    private final ResolvableType fieldClass;


    /**
     * @param type the class containing the field or JavaBean property
     * @param name  the property/field name
     */
    @SuppressWarnings("unchecked")
    public Setter(ResolvableType type, String name) {
        BiConsumer<T, F> tempSetter    = null;
        ResolvableType   rawFieldClass = null;
        Class<?> clazz = Objects.requireNonNull(type.resolve());

        // 1) Try bean-style accessors
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (pd.getName().equals(name)) {
                    Method write = pd.getWriteMethod();
                    if (write != null) write.setAccessible(true);
                    if (write != null) {
                        rawFieldClass = ResolvableType.forMethodParameter(write, 0);
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
                                rawFieldClass == null || rawFieldClass.isAssignableFrom(ResolvableType.forMethodParameter(m, 0))
                        )) {
                    log.info("Found {}", m);
                    rawFieldClass = ResolvableType.forMethodParameter(m, 0);
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
                rawFieldClass = ResolvableType.forField(field);

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
        ResolvableType finalFieldClass = PrimitiveUtil.wrapPrimitive(rawFieldClass);


        this.setter     = tempSetter;
        this.fieldClass = finalFieldClass;
    }

    /** Write the named property/field on the given target instance. */
    public void set(T target, F value) {
        setter.accept(target, value);
    }

    /** Returns the `Class` object representing the field’s (wrapped) type. */
    public ResolvableType getFieldClass() {
        return fieldClass;
    }

    @Override
    public String toString() {
        return "Setter{" +
                "fieldClass=" + fieldClass +
                '}';
    }
}
