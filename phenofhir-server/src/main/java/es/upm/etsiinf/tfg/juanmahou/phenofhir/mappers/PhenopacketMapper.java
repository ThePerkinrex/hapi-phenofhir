package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.Phenopacket;
import entities.org.phenopackets.schema.v2.core.Disease;
import entities.org.phenopackets.schema.v2.core.Individual;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRunner;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction.Resolver;
import org.hl7.fhir.r4b.model.Reference;
import org.hl7.fhir.r4b.model.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

@Component
public class PhenopacketMapper implements MapperClass {
    private static final Logger log = LoggerFactory.getLogger(PhenopacketMapper.class);
    private final ObjectProvider<Resolver> resolverObjectProvider;
    private final ObjectProvider<MapperRegistry> mapperRegistry;

    public PhenopacketMapper(ObjectProvider<Resolver> resolverObjectProvider, ObjectProvider<MapperRegistry> mapperRegistry) {
        this.resolverObjectProvider = resolverObjectProvider;
        this.mapperRegistry = mapperRegistry;
    }

    @Mapper
    public void resolveIndividual(Context<Individual> ctx, Reference ref) {
        log.info("Mapping reference for individual: {}", ref);
        Resolver resolver = resolverObjectProvider.getObject();
        var resolved = resolver.get(ref.getReference());
        if(resolved == null) throw new RuntimeException("Referenced resource for patient " + ref.getReference() + " not found");
        log.info("Resolved: {}", resolved);
        resolved.onBuild(o -> {
            if(o instanceof Individual id) {
                ctx.accept(id);
            }else{
                throw new RuntimeException("Expected Individual, found " + ResolvableType.forInstance(o));
            }
        });

    }

    @Mapper
    public void resolveDiseases(Context<Set<Disease>> ctx, List<Reference> references) throws Exception {
        MapperRegistry registry = this.mapperRegistry.getObject();
        Resolver resolver = resolverObjectProvider.getObject();

        Consumer<Disease> consumer = new Consumer<Disease>() {
            private final Set<Disease> res = new HashSet<>();
            @Override
            public void accept(Disease disease) {
                res.add(disease);
                if(res.size() == references.size()) {
                    ctx.accept(res);
                }
            }
        };
        for(var ref : references) {
            var resolved = resolver.get(ref.getReference());
            if(resolved == null) throw new RuntimeException("Referenced resource for condition " + ref.getReference() + " not found");
            if(resolved.getPheno() instanceof Disease dis) {
                consumer.accept(dis);
            }else{
                Resource resource = resolved.getResource();
                MapperRunner runner = registry.getMapper(ResolvableType.forClass(Disease.class), List.of(ResolvableType.forInstance(resource)));
                if(runner == null) throw new RuntimeException("No mapper for diseases found; source: " + ResolvableType.forInstance(resource));
                runner.run(ctx, List.of(resource), x -> {}, x -> consumer.accept((Disease) x));
            }
        }
    }

    @Mapper
    public Phenopacket fromKey(Context<?> ctx, Phenopacket.Key key) {
        return new Phenopacket().setId(key);
    }
}
