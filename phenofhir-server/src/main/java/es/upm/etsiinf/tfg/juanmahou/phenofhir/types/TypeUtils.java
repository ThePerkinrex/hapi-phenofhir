package es.upm.etsiinf.tfg.juanmahou.phenofhir.types;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;

import java.lang.reflect.*;
import java.util.List;

public class TypeUtils {
    /**
     * Resolve a java.lang.reflect.Type to a Class<?>.
     *
     * @param type the type to resolve
     * @return the corresponding raw class
     * @throws IllegalArgumentException if the type cannot be resolved to a Class
     */
    public static Class<?> toClass(Type type) {
        if (type instanceof Class<?>) {
            // simple class or primitive
            return (Class<?>) type;
        }
        else if (type instanceof ParameterizedType) {
            // e.g. List<String> → List.class
            return toClass(((ParameterizedType) type).getRawType());
        }
        else if (type instanceof GenericArrayType) {
            // e.g. T[] where T is a type variable → create array of component and get its class
            Type compType = ((GenericArrayType) type).getGenericComponentType();
            Class<?> compClass = toClass(compType);
            // Array.newInstance(...) returns Object, but getClass() gives us the array class
            return Array.newInstance(compClass, 0).getClass();
        }
        else if (type instanceof TypeVariable<?>) {
            // e.g. T extends Number → use the first bound
            Type[] bounds = ((TypeVariable<?>) type).getBounds();
            return toClass(bounds.length > 0 ? bounds[0] : Object.class);
        }
        else if (type instanceof WildcardType) {
            // e.g. ? extends Number → use upper bound
            Type[] bounds = ((WildcardType) type).getUpperBounds();
            return toClass(bounds.length > 0 ? bounds[0] : Object.class);
        }
        else {
            throw new IllegalArgumentException("Cannot convert Type to Class: " + type);
        }
    }

    public static boolean isAssignableFrom(Type rawFieldClass, Type genericParameterType) {
        return toClass(rawFieldClass).isAssignableFrom(toClass(genericParameterType));
    }
}