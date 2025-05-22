package es.upm.etsiinf.tfg.juanmahou.plugin.render;

import es.upm.etsiinf.tfg.juanmahou.plugin.tables.field.Field;
import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.JavaType;

import java.util.List;
import java.util.Objects;

public class Accessor {
    private final Field field;

    public Accessor(Field field) {
        this.field = field;
    }

    public Field getField() {
        return field;
    }

    public JavaType getType() {
        return field.getType();
    }

    public String getGetterName() {
        String name = field.getName();
        JavaType type = getType();

        // For boolean properties use 'is', otherwise 'get'
        boolean isBoolean = ("boolean".equals(type.toString()));
        String prefix = isBoolean ? "is" : "get";

        return prefix + capitalizeBeanProperty(name);
    }

    public String getSetterName() {
        return "set" + capitalizeBeanProperty(field.getName());
    }

    public String getName() {
        return field.getName();
    }

    /**
     * Capitalizes a property name according to the JavaBean spec:
     * If the first two characters are both uppercase, leave it alone;
     * otherwise uppercase only the first character.
     */
    private static String capitalizeBeanProperty(String name) {
        if (name == null || name.isEmpty()) {
            return name;
        }
        if (name.length() > 1 && Character.isUpperCase(name.charAt(0))
                && Character.isUpperCase(name.charAt(1))) {
            // e.g. "URL" -> "URL"
            return name;
        }
        // e.g. "foo" -> "Foo", "x" -> "X"
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public List<Field> getToNull() {
        if(field.getOneOf() == null) return List.of();
        return field
                .getOneOf()
                .getFields()
                .stream()
                .filter(g -> !Objects.equals(g, field.getOneOfGroup()))
                .flatMap(List::stream)
                .toList();
    }
}
