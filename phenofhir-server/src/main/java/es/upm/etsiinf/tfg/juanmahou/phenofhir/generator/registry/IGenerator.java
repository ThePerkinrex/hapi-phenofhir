package es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public interface IGenerator<T, C> {
    Class<C> getConfigClass();
    Class<T> getTargetClass();
    T generate(C config, GeneratorContext ctx);

    class ConfiguredGenerator<T, C> {
        private final IGenerator<T, C> generator;
        private final C config;

        public ConfiguredGenerator(IGenerator<T, C> generator, C config) {
            this.generator = generator;
            this.config = config;
        }

        public Class<T> getTargetClass() {
            return this.generator.getTargetClass();
        }

        public T generate(GeneratorContext ctx) {
            return this.generator.generate(config, ctx);
        }
    }
}
