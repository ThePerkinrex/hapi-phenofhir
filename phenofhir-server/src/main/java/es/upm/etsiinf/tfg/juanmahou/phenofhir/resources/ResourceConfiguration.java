package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class ResourceConfiguration {
    private static final Logger log = LoggerFactory.getLogger(ResourceConfiguration.class);

    @Bean
    public List<GeneralPhenomicResource> resources(Config config, MapperRegistry registry) throws ClassNotFoundException, NoSuchMethodException {
        log.info("Getting mappings {}", config);
        List<ResourceMapping<?, IBaseResource>> resources = new ArrayList<>(config.getMappings().size());

        for (var e : config.getMappings().entrySet()) {
            String name = e.getKey();
            Mapping mapping = e.getValue();
            log.info("Loading provider for {}", name);


            Class<?> resource = getClass().getClassLoader().loadClass("org.hl7.fhir.r4b.model." + name);
            if(!IBaseResource.class.isAssignableFrom(resource)) throw new ClassNotFoundException(name + " is not a valid resource");
            Class<?> target = getClass().getClassLoader().loadClass(mapping.getTarget());
            ResourceMapping<?, IBaseResource> m = new ResourceMapping<>(target, (Class<? extends IBaseResource>) resource, mapping, registry);
            registry.registerMapper(m);
            resources.add(m);
        }

        return resources.stream().map(m -> {
            m.initialize();
            try {
                return new GeneralPhenomicResource(m.getPhenoClass(), m.getFhirClass(), m.getMapping(), m);
            } catch (ClassNotFoundException | NoSuchMethodException e) {
                throw new RuntimeException(e);
            }
        }).toList();
    }
}
