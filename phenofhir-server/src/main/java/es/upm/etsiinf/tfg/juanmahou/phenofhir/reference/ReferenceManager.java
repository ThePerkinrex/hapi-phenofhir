package es.upm.etsiinf.tfg.juanmahou.phenofhir.reference;

import ca.uhn.fhir.context.FhirContext;
import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import org.hl7.fhir.instance.model.api.IBaseResource;
import org.hl7.fhir.r4.model.BaseResource;
import org.hl7.fhir.r4.model.IdType;
import org.hl7.fhir.r4.model.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class ReferenceManager {
    private static final Logger log = LoggerFactory.getLogger(ReferenceManager.class);
    private final FhirContext ctx;
    private final ObjectProvider<MapperRegistry> mapperRegistry;
    private final ConcurrentMap<ResolvableType, String> idClassCache = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, ResolvableType> idTypeCache = new ConcurrentHashMap<>();

    public ReferenceManager(FhirContext ctx, ObjectProvider<MapperRegistry> mapperRegistry) {
        this.ctx = ctx;
        this.mapperRegistry = mapperRegistry;
    }

    private String getTypeForClass(ResolvableType type) {
        return idClassCache.computeIfAbsent(type, t -> {
            MapperRegistry mapperRegistry = ReferenceManager.this.mapperRegistry.getObject();
            List<MapperRegistry.MapperAndData> mappers = mapperRegistry
                    .getAllForArgs(List.of(t))
                    .filter(m -> !m.key().ret().as(BaseResource.class).equalsType(ResolvableType.NONE))
                    .toList();
            if(mappers.isEmpty()) {
                log.warn("No mapper found for {}", t);
                throw new RuntimeException();
            } else if (mappers.size() > 1) {
                log.warn("Too many mappers found for {}", t);
                throw new RuntimeException();
            }
            return ctx.getResourceType((Class<? extends IBaseResource>) mappers.getFirst().key().ret().toClass());
        });
    }

    private ResolvableType getClassForType(String type) {
        return idTypeCache.computeIfAbsent(type, t -> {
            Class<?> cls = ctx.getResourceDefinition(type).getImplementingClass();
            MapperRegistry mapperRegistry = ReferenceManager.this.mapperRegistry.getObject();
            List<MapperRegistry.MapperAndData> mappers = mapperRegistry
                    .getAllForArgs(List.of(ResolvableType.forClass(cls)))
                    .filter(m -> !m.key().ret().as(WithId.class).equalsType(ResolvableType.NONE))
                    .toList();
            if(mappers.isEmpty()) {
                log.warn("No mapper found for {}", t);
                throw new RuntimeException();
            } else if (mappers.size() > 1) {
                log.warn("Too many mappers found for {}", t);
                throw new RuntimeException();
            }
            return mappers.getFirst().key().ret();
        });
    }

    public <I extends Id> Reference getReference(WithId<I> obj) {
        return new Reference(new IdType(getTypeForClass(ResolvableType.forInstance(obj)), obj.getId().toString()));
    }

    public Id getIdForRef(Reference ref) throws IOException {
        ResolvableType rt = getClassForType(ref.getType()).as(WithId.class).getGeneric(0);
        return Id.fromString(ref.getReference(), (Class<? extends Id>) rt.toClass());
    }
}
