package es.upm.etsiinf.tfg.juanmahou.phenofhir.resources;

import ca.uhn.fhir.rest.server.IResourceProvider;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRunner;
import es.upm.etsiinf.tfg.juanmahou.mapper.TypeRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.KeyUtils;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class GeneralPhenomicResources {
    private static final Logger log = LoggerFactory.getLogger(GeneralPhenomicResources.class);

    private final Map<ResolvableType, IResourceProvider> resources;

    public GeneralPhenomicResources(
            MapperRegistry registry,
            ObjectProvider<GeneralPhenomicResource<? extends Id, ? extends WithId<?>>> provider,
            RepositoryProvider repositoryProvider,
            KeyUtils keyUtils
    ) {
        this.resources = new HashMap<>();

        List<MapperRegistry.MapperAndData> mappers = registry
                .getAll()
                .filter(x -> x.key().params().size() == 1 &&
                                            !x.key().params().getFirst().as(WithId.class).equalsType(ResolvableType.NONE) &&
                                            !x.key().ret().as(IBaseResource.class).equalsType(ResolvableType.NONE))
                .toList();

        for (MapperRegistry.MapperAndData m : mappers) {
            ResolvableType source = m.key().params().getFirst();
            ResolvableType target = m.key().ret();

            if (!ResolvableType.forClass(IBaseResource.class).isAssignableFrom(target)) {
                log.warn("{} is not a resource, skipping", target);
                continue;
            }

            MapperRunner runner = m.runner();
            if (runner == null) throw new RuntimeException("Runner is unexpectedly null");

            GeneralPhenomicResource<? extends Id, ? extends WithId<?>> gpr = provider.getObject(
                    source,
                    target,
                    runner,
                    repositoryProvider,
                    registry,
                    keyUtils
            );
            this.resources.put(target, gpr);
        }

//        for (var e : config.getMappings().entrySet()) {
//            String name = e.getKey();
//            Mapping mapping = e.getValue();
//            log.info("Loading provider for {}", name);
//
//
//            Class<?> resource = getClass().getClassLoader().loadClass("org.hl7.fhir.r4b.model." + name);
//            if(!IBaseResource.class.isAssignableFrom(resource)) throw new ClassNotFoundException(name + " is not a
//            valid resource");
//            Class<?> target = getClass().getClassLoader().loadClass(mapping.getTarget());
//            if(!WithId.class.isAssignableFrom(target)) throw new ClassNotFoundException(mapping.getTarget() + " is
//            not a valid phenopacket entity");
//            @SuppressWarnings("unchecked")
//            Class<? extends WithId<? extends Id>> castedTarget = (Class<? extends WithId<? extends Id>>) target;
//            @SuppressWarnings("unchecked")
//            Class<? extends IBaseResource> castedResource = (Class<? extends IBaseResource>) resource;
//            ToFhirConfiguredMapping<?, IBaseResource> toFhirConfiguredMapping = new ToFhirConfiguredMapping<>
//            (castedTarget, castedResource, mapping, registry);
//            ToPhenoConfiguredMapping<?, IBaseResource> toPhenoConfiguredMapping = new ToPhenoConfiguredMapping<>
//            (castedTarget, castedResource, mapping, registry, requestGeneratorContextObjectProvider);
////            ResourceMapping<?, IBaseResource> m = new ResourceMapping<>(
////                    (Class<? extends WithId<? extends Id>>) target,
////                    (Class<? extends IBaseResource>) resource,
////                    mapping,
////                    registry,
////                    requestGeneratorContextObjectProvider);
//            PhenoMapper<?, ?> phenoMapper = registry.registerMapper(toPhenoConfiguredMapping);
//            resources.add(new WithMapping<>(registry.registerMapper(toFhirConfiguredMapping), () -> {
//                try {
//                    toPhenoConfiguredMapping.initialize();
//                } catch (NotFoundException ex) {
//                    log.error("Error initializing toPheno for {} <- {}", castedTarget, castedResource, ex);
//                    registry.unregisterMapper(phenoMapper);
//                }
//                toFhirConfiguredMapping.initialize();
//            }, mapping));
//        }
//
//
//        this.resources = resources
//                .stream()
//                .map(withMapping -> {
//                    try {
//                        withMapping.initialize().initialize();
//                        var m = withMapping.data();
//                        return new WithMapping<>(provider.getObject(m.getPhenoClass(), m.getFhirClass(),
//                        withMapping.mapping(), m, repositoryProvider, registry), withMapping.initialize(),
//                        withMapping.mapping());
//                    }catch (NotFoundException e) {
//                        return null;
//                    }
//                })
//                .filter(Objects::nonNull)
//                .collect(Collectors.toMap(withMapping -> withMapping.data().getResourceType(), WithMapping::data));
    }

    public List<IResourceProvider> getResources() {
        return resources.values().stream().toList();
    }

    public IResourceProvider get(ResolvableType c) {
        return resources.get(c);
    }
}
