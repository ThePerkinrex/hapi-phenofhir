package es.upm.etsiinf.tfg.juanmahou.plugin.types;

public interface JavaType {
    boolean equals(JavaType other);

    public static boolean equals(JavaType a, JavaType b) {
        if (a == null) return b == null;
        return a.equals(b);
    }
}
