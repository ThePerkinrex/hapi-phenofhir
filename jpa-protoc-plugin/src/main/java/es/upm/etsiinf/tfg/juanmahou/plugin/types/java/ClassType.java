package es.upm.etsiinf.tfg.juanmahou.plugin.types.java;

import java.util.Objects;

public class ClassType implements JavaType {
    private final String packageName;
    private final String name;

    public ClassType(String packageName, String name) {
        this.packageName = packageName;
        this.name = name;
    }

    /**
     * Constructor from a Class<?> object.
     */
    public ClassType(Class<?> clazz) {
        Objects.requireNonNull(clazz);
        Package pkg = clazz.getPackage();
        this.packageName = (pkg != null ? pkg.getName() : "");
        this.name = clazz.getSimpleName();
    }

    public String getName() {
        return name;
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public String toString() {
        return packageName + "." + name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ClassType classType = (ClassType) o;
        return Objects.equals(packageName, classType.packageName) && Objects.equals(name, classType.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(packageName, name);
    }

    @Override
    public boolean equals(JavaType other) {
        return this.equals((Object) other);
    }
}
