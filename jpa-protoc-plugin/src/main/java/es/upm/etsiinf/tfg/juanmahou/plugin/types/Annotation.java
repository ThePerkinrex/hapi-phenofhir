package es.upm.etsiinf.tfg.juanmahou.plugin.types;

import java.util.function.Supplier;

public record Annotation(JavaType annotationType, Supplier<String> extra) {
    public Annotation(JavaType annotationType, Supplier<String> extra) {
        this.annotationType = annotationType;
        this.extra = extra;
    }

    public Annotation(JavaType annotationType) {
        this(annotationType, null);
    }

    public String toString() {
        return "@" + annotationType().toString() + (extra() == null ? "" : "(" + extra().get() + ")");
    }
}
