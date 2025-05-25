package es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class GeneratorRegistry {
    private final Map<String, IGenerator<?, ?>> generators;

    public GeneratorRegistry(List<IGenerator<?, ?>> generators) {
        this.generators = generators.stream()
                .filter(g -> g.getClass().isAnnotationPresent(Generator.class))
                .collect(Collectors.toMap(
                        g -> g.getClass().getAnnotation(Generator.class).value(),
                        g -> g));
    }

    public IGenerator<?, ?> get(String name) {
        return generators.get(name);
    }
}
