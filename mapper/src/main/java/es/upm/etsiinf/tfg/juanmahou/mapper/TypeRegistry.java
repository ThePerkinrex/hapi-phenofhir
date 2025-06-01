package es.upm.etsiinf.tfg.juanmahou.mapper;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ScanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
public class TypeRegistry {
    private static final Logger log = LoggerFactory.getLogger(TypeRegistry.class);

    private final Map<String, ResolvableType> aliases;

    public TypeRegistry() {
        aliases = new HashMap<>();
        try (ScanResult scan = new ClassGraph()
                // only scan that one package:
                .acceptPackages("org.hl7.fhir.r4.model")
                // get all classes (you can also enableAnnotationInfo(), etc.):
                .enableClassInfo()
                .scan()) {

            // loadClasses() gives you real java.lang.Class<?> objects
            for(Class<?> c :  scan.getAllStandardClasses().loadClasses()) {
                aliases.put(c.getSimpleName(), ResolvableType.forClass(c));
            }
        }
    }

    public ResolvableType resolve(String name) {
        ResolvableType rt = aliases.get(name);
        if(rt != null) return rt;
        try {
            return ResolvableType.forClass(getClass().getClassLoader().loadClass(name));
        } catch (ClassNotFoundException e) {
            log.error("Could not find class {}", name);
            return null;
        }
    }
}
