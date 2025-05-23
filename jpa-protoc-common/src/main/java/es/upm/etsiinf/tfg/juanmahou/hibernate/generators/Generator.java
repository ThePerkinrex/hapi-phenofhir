package es.upm.etsiinf.tfg.juanmahou.hibernate.generators;

import io.github.classgraph.ClassInfo;
import io.github.classgraph.MethodInfo;
import io.github.classgraph.MethodInfoList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class Generator {
    private static final Logger log = LoggerFactory.getLogger(Generator.class);
    private final ClassInfo annotationInfo;

    Generator(ClassInfo annotationInfo) {
        this.annotationInfo = annotationInfo;
    }

    public Class<? extends Annotation> getAnnotationClass() {
        return annotationInfo.loadClass(Annotation.class);
    }

    public boolean validateParams(Map<String, Object> params) {
// 1) Get all elements of the annotation
        MethodInfoList methods = annotationInfo.getMethodInfo();
        Set<String> allNames = methods.stream()
                .map(MethodInfo::getName)
                .collect(Collectors.toSet());

        // 2) Identify which elements are *required* (i.e. have no default)
        Set<String> requiredNames = methods.stream()
                .filter(mi -> {
                    // load the actual java.lang.reflect.Method
                    Method m = mi.loadClassAndGetMethod();
                    // Method.getDefaultValue() returns null if there's no default :contentReference[oaicite:0]{index=0}
                    return m.getDefaultValue() == null;

                })
                .map(MethodInfo::getName)
                .collect(Collectors.toSet());

        // 3) Ensure all required names are present
        if (!params.keySet().containsAll(requiredNames)) {
            log.warn("Missing required elements: {} (got {})",
                    requiredNames, params.keySet());
            return false;
        }
        // 4) Ensure no extra keys
        if (!allNames.containsAll(params.keySet())) {
            log.warn("Unexpected elements: {} (allowed {})",
                    params.keySet(), allNames);
            return false;
        }
        // 5) Type-check each provided value
        for (MethodInfo mi : methods) {
            String name = mi.getName();
            if (!params.containsKey(name)) {
                // it’s missing, but we know it has a default, so skip it
                continue;
            }
            Object val = params.get(name);
            Class<?> expected = mi.loadClassAndGetMethod().getReturnType();
            if (isNotCompatible(val, expected)) {
                log.warn("Type mismatch for '{}': expected {}, got {}",
                        name,
                        expected.getSimpleName(),
                        val == null ? "null" : val.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }

    private boolean isNotCompatible(Object val, Class<?> expectedType) {
        if (val == null) return true;
        // Handle primitives
        if (expectedType.isPrimitive()) {
            expectedType = primitiveToWrapper(expectedType);
        }
        // Handle arrays
        if (expectedType.isArray()) {
            if (!val.getClass().isArray()) return true;
            Class<?> compType = expectedType.getComponentType();
            int len = java.lang.reflect.Array.getLength(val);
            for (int i = 0; i < len; i++) {
                Object e = java.lang.reflect.Array.get(val, i);
                if (isNotCompatible(e, compType)) return true;
            }
            return false;
        }
        // Enums, Class<?>, annotations, etc.
        return !expectedType.isInstance(val);
    }

    private Class<?> primitiveToWrapper(Class<?> prim) {
        return switch (prim.getName()) {
            case "boolean" -> Boolean.class;
            case "byte" -> Byte.class;
            case "char" -> Character.class;
            case "double" -> Double.class;
            case "float" -> Float.class;
            case "int" -> Integer.class;
            case "long" -> Long.class;
            case "short" -> Short.class;
            default -> prim;
        };
    }
}
