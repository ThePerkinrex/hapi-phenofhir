package es.upm.etsiinf.tfg.juanmahou.plugin.types;

import es.upm.etsiinf.tfg.juanmahou.plugin.types.java.JavaType;

import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public record Annotation(JavaType annotationType, List<String> extra) {
    public Annotation(JavaType annotationType, List<String> extra) {
        this.annotationType = annotationType;
        this.extra = extra;
    }

    public Annotation(JavaType annotationType) {
        this(annotationType, null);
    }

    public String toString() {
        return "@" + annotationType().toString() + ((extra == null || extra.isEmpty()) ? "" : "(" + String.join(", ", extra) + ")");
    }
}
