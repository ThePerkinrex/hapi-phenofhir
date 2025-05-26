package es.upm.etsiinf.tfg.juanmahou.mapper.field;

import es.upm.etsiinf.tfg.juanmahou.mapper.util.PrimitiveUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.function.Function;

public class Getter<T, F> {
    private static final Logger log = LoggerFactory.getLogger(Getter.class);
    private final Function<T, F> getter;
    private final ResolvableType fieldClass;

    /**
     * @param type the class containing the field or JavaBean property
     * @param name  the property/field name
     */
    @SuppressWarnings("unchecked")
    public Getter(ResolvableType type, String name) {
        Function<T, F>   tempGetter     = null;
        ResolvableType    rawFieldClass = null;
        Class<?> clazz = Objects.requireNonNull(type.resolve());

        // 1) Try bean-style accessors
        try {
            for (PropertyDescriptor pd : Introspector.getBeanInfo(clazz).getPropertyDescriptors()) {
                if (pd.getName().equals(name)) {
                    Method read  = pd.getReadMethod();
                    if (read  != null) read .setAccessible(true);

                    if (read != null) {
                        rawFieldClass = ResolvableType.forMethodReturnType(read);
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
                rawFieldClass = ResolvableType.forField(field);
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
        ResolvableType finalFieldClass = PrimitiveUtil.wrapPrimitive(rawFieldClass);


        this.getter     = tempGetter;
        this.fieldClass = finalFieldClass;
    }

    /** Read the named property/field from the given target instance. */
    public F get(T target) {
        return getter.apply(target);
    }

    /** Returns the `Class` object representing the field’s (wrapped) type. */
    public ResolvableType getFieldClass() {
        return fieldClass;
    }

    @Override
    public String toString() {
        return "Getter{" +
                "fieldClass=" + fieldClass +
                '}';
    }
}
