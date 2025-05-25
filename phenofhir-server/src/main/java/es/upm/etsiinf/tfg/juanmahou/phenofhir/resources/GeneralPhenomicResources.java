package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import ca.uhn.fhir.rest.server.IResourceProvider;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.GeneratorContext;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.RequestGeneratorContext;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.FhirMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.NotFoundException;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.mapper.Initializable;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.mapper.ToFhirConfiguredMapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.resources.mapper.ToPhenoConfiguredMapping;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class GeneralPhenomicResources {
    private static final Logger log = LoggerFactory.getLogger(GeneralPhenomicResources.class);

    private record WithMapping<T>(T data, Initializable initialize, Mapping mapping) {}

    private final Map<Type, IResourceProvider> resources;

    public GeneralPhenomicResources(
            Config config,
            MapperRegistry registry,
            ObjectProvider<GeneralPhenomicResource<? extends Id, ? extends WithId<?>, ? extends IBaseResource>> provider,
            RepositoryProvider repositoryProvider,
            ObjectProvider<GeneratorContext> requestGeneratorContextObjectProvider
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
            @SuppressWarnings("unchecked")
            Class<? extends WithId<? extends Id>> castedTarget = (Class<? extends WithId<? extends Id>>) target;
            @SuppressWarnings("unchecked")
            Class<? extends IBaseResource> castedResource = (Class<? extends IBaseResource>) resource;
            ToFhirConfiguredMapping<?, IBaseResource> toFhirConfiguredMapping = new ToFhirConfiguredMapping<>(castedTarget, castedResource, mapping, registry);
            ToPhenoConfiguredMapping<?, IBaseResource> toPhenoConfiguredMapping = new ToPhenoConfiguredMapping<>(castedTarget, castedResource, mapping, registry, requestGeneratorContextObjectProvider);
//            ResourceMapping<?, IBaseResource> m = new ResourceMapping<>(
//                    (Class<? extends WithId<? extends Id>>) target,
//                    (Class<? extends IBaseResource>) resource,
//                    mapping,
//                    registry,
//                    requestGeneratorContextObjectProvider);
            PhenoMapper<?, ?> phenoMapper = registry.registerMapper(toPhenoConfiguredMapping);
            resources.add(new WithMapping<>(registry.registerMapper(toFhirConfiguredMapping), () -> {
                try {
                    toPhenoConfiguredMapping.initialize();
                } catch (NotFoundException ex) {
                    log.error("Error initializing toPheno for {} <- {}", castedTarget, castedResource, ex);
                    registry.unregisterMapper(phenoMapper);
                }
                toFhirConfiguredMapping.initialize();
            }, mapping));
        }


        this.resources = resources
                .stream()
                .map(withMapping -> {
                    try {
                        withMapping.initialize().initialize();
                        var m = withMapping.data();
                        return new WithMapping<>(provider.getObject(m.getPhenoClass(), m.getFhirClass(), withMapping.mapping(), m, repositoryProvider, registry), withMapping.initialize(), withMapping.mapping());
                    }catch (NotFoundException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(withMapping -> withMapping.data().getResourceType(), WithMapping::data));
    }

    public List<IResourceProvider> getResources() {
        return resources.values().stream().toList();
    }

    public IResourceProvider get(Type c) {
        return resources.get(c);
    }
}
