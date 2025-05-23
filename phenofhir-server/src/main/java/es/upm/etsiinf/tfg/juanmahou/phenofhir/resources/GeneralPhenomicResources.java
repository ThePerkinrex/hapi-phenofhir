package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import ca.uhn.fhir.rest.server.IResourceProvider;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.id.IdMapper;
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

    private final List<IResourceProvider> resources;

    public GeneralPhenomicResources(
            Config config,
            MapperRegistry registry,
            ObjectProvider<GeneralPhenomicResource<? extends Id, ? extends WithId<?>, ? extends IBaseResource>> provider,
            IdMapper idMapper,
            RepositoryProvider repositoryProvider
    ) throws ClassNotFoundException, NoSuchMethodException {
        log.info("Getting mappings {}", config);
        List<WithMapping<FhirMapper<?, IBaseResource>>> resources = new ArrayList<>(config.getMappings().size());

        for (var e : config.getMappings().entrySet()) {
            String name = e.getKey();
            Mapping mapping = e.getValue();
            log.info("Loading provider for {}", name);


            Class<?> resource = getClass().getClassLoader().loadClass("org.hl7.fhir.r4b.model." + name);
            if(!IBaseResource.class.isAssignableFrom(resource)) throw new ClassNotFoundException(name + " is not a valid resource");
            Class<?> target = getClass().getClassLoader().loadClass(mapping.getTarget());
            if(!WithId.class.isAssignableFrom(target)) throw new ClassNotFoundException(mapping.getTarget() + " is not a valid phenopacket entity");
            ResourceMapping<?, IBaseResource> m = new ResourceMapping<>(
                    (Class<? extends WithId<? extends Id>>) target,
                    (Class<? extends IBaseResource>) resource,
                    mapping,
                    registry);
            resources.add(new WithMapping<>(registry.registerMapper(m), m::initialize, mapping));
        }


        this.resources = resources.stream().map(withMapping -> {
            withMapping.initialize().run();
            var m = withMapping.data();
            return (IResourceProvider) provider.getObject(m.getPhenoClass(), m.getFhirClass(), withMapping.mapping(), m, idMapper, repositoryProvider);
        }).toList();
    }

    public List<IResourceProvider> getResources() {
        return resources;
    }
}
