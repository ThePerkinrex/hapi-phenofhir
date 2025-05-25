package es.upm.etsiinf.tfg.juanmahou.phenofhir.types;

import org.hl7.fhir.r4b.model.Identifier;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ListType implements ParameterizedType {
    private final Type inner;

    public ListType(Type inner) {
        this.inner = inner;
    }

    /**
     * Returns an array of {@code Type} objects representing the actual type
     * arguments to this type.
     *
     * <p>Note that in some cases, the returned array be empty. This can occur
     * if this type represents a non-parameterized type nested within
     * a parameterized type.
     *
     * @return an array of {@code Type} objects representing the actual type
     * arguments to this type
     * @throws TypeNotPresentException             if any of the actual type arguments
     *                                             refers to a non-existent class or interface declaration
     * @throws MalformedParameterizedTypeException if any of the
     *                                             actual type parameters refer to a parameterized type that cannot
     *                                             be instantiated for any reason
     * @since 1.5
     */
    @Override
    public Type[] getActualTypeArguments() {
        return new Type[]{inner};
    }

    /**
     * Returns the {@code Type} object representing the class or interface
     * that declared this type.
     *
     * @return the {@code Type} object representing the class or interface
     * that declared this type
     * @since 1.5
     */
    @Override
    public Type getRawType() {
        return List.class;
    }

    /**
     * Returns a {@code Type} object representing the type that this type
     * is a member of.  For example, if this type is {@code O<T>.I<S>},
     * return a representation of {@code O<T>}.
     *
     * <p>If this type is a top-level type, {@code null} is returned.
     *
     * @return a {@code Type} object representing the type that
     * this type is a member of. If this type is a top-level type,
     * {@code null} is returned
     * @throws TypeNotPresentException             if the owner type
     *                                             refers to a non-existent class or interface declaration
     * @throws MalformedParameterizedTypeException if the owner type
     *                                             refers to a parameterized type that cannot be instantiated
     *                                             for any reason
     * @since 1.5
     */
    @Override
    public Type getOwnerType() {
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof ParameterizedType pt) {
            return Objects.equals(pt.getRawType(), getRawType()) && Arrays.equals(pt.getActualTypeArguments(), getActualTypeArguments()) && Objects.equals(pt.getOwnerType(), getOwnerType());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(inner);
    }

    @Override
    public String toString() {
        return List.class.getTypeName() + "<" + inner.getTypeName() + ">";
    }
}
