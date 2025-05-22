package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.Mapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class GeneralPhenomicResources {
    private static final Logger log = LoggerFactory.getLogger(GeneralPhenomicResources.class);

    private record WithMapping<T>(T data, Runnable initialize, Mapping mapping) {}

    private final List<GeneralPhenomicResource> resources;

    public GeneralPhenomicResources(Config config, MapperRegistry registry, ObjectProvider<GeneralPhenomicResource> provider) throws ClassNotFoundException, NoSuchMethodException {
        log.info("Getting mappings {}", config);
        List<WithMapping<Mapper<?, IBaseResource>>> resources = new ArrayList<>(config.getMappings().size());

        for (var e : config.getMappings().entrySet()) {
            String name = e.getKey();
            Mapping mapping = e.getValue();
            log.info("Loading provider for {}", name);


            Class<?> resource = getClass().getClassLoader().loadClass("org.hl7.fhir.r4b.model." + name);
            if(!IBaseResource.class.isAssignableFrom(resource)) throw new ClassNotFoundException(name + " is not a valid resource");
            Class<?> target = getClass().getClassLoader().loadClass(mapping.getTarget());
            ResourceMapping<?, IBaseResource> m = new ResourceMapping<>(target, (Class<? extends IBaseResource>) resource, mapping, registry);
            resources.add(new WithMapping<>(registry.registerMapper(m), m::initialize, mapping));
        }


        this.resources = resources.stream().map(withMapping -> {
            withMapping.initialize().run();
            var m = withMapping.data();
            return provider.getObject(m.getPhenoClass(), m.getFhirClass(), withMapping.mapping(), m);
        }).toList();
    }

    public List<GeneralPhenomicResource> getResources() {
        return resources;
    }
}
