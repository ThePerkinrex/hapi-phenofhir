package es.upm.etsiinf.tfg.juanmahou.hibernate.generators;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import io.github.classgraph.ScanResult;
import org.hibernate.annotations.IdGeneratorType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class GeneratorRegistry {
    private static final Logger log = LoggerFactory.getLogger(GeneratorRegistry.class);
    private static GeneratorRegistry instance = null;
    public static GeneratorRegistry getInstance() {
        if (instance == null) {
            instance = new GeneratorRegistry();
        }
        return instance;
    }

    private final Map<String, Generator> aliasedAnnotations;


    private GeneratorRegistry() {
        aliasedAnnotations = new HashMap<>();
        try (ScanResult result = new ClassGraph().enableAllInfo().scan()) {
            for (ClassInfo ann : result.getClassesWithAnnotation(GeneratorAlias.class)) {
                if (!ann.hasAnnotation(IdGeneratorType.class)) {
                    log.warn("Annotation {} was marked as a generator alias, but does not include {}", ann.getName(), IdGeneratorType.class);
                    continue;
                }
                GeneratorAlias alias = (GeneratorAlias) ann.getAnnotationInfo(GeneratorAlias.class).loadClassAndInstantiate();
                aliasedAnnotations.put(alias.value(), new Generator(ann));
            }
        }
    }

    public Generator getGenerator(String name) throws GeneratorNotFoundException {
        Generator ann = aliasedAnnotations.get(name);
        if (ann != null) {
            return ann;
        }
        try (ScanResult result = new ClassGraph().enableAllInfo().acceptClasses(name).scan()) {
            for (ClassInfo cls : result.getClassesWithAnnotation(IdGeneratorType.class)) {
                Generator gen = new Generator(cls);
                aliasedAnnotations.put(name, gen); // Cache it
                return gen;
            }
        }
        throw new GeneratorNotFoundException(name);
    }
}
