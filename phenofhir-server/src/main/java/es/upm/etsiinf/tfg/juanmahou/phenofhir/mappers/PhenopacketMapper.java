package es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers;

import entities.org.phenopackets.schema.v2.Phenopacket;
import entities.org.phenopackets.schema.v2.core.Disease;
import entities.org.phenopackets.schema.v2.core.Individual;
import entities.org.phenopackets.schema.v2.core.MetaData;
import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperClass;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.MapperRunner;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.CurieManager;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.reference.ReferenceManager;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.transaction.Resolver;
import org.hl7.fhir.r4b.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;

@Component
public class PhenopacketMapper implements MapperClass {
    private static final Logger log = LoggerFactory.getLogger(PhenopacketMapper.class);
    private final ObjectProvider<Resolver> resolverObjectProvider;
    private final ObjectProvider<MapperRegistry> mapperRegistry;
    private final ReferenceManager referenceManager;

    public PhenopacketMapper(ObjectProvider<Resolver> resolverObjectProvider, ObjectProvider<MapperRegistry> mapperRegistry, ReferenceManager referenceManager) {
        this.resolverObjectProvider = resolverObjectProvider;
        this.mapperRegistry = mapperRegistry;
        this.referenceManager = referenceManager;
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
                runner.run(ctx, List.of(resource), x -> {}, x -> {
                    Disease d = (Disease) x;
                    resolved.setPheno(d);
                    consumer.accept(d);
                });
            }
        }
    }

    @Mapper
    public MetaData getMetaData(Context<?> ctx, List<Reference> author, Date created) {
        // TODO Use author list
        return new MetaData().setId(new MetaData.Key())
                .setCreated(created.toInstant())
                .setPhenopacket_schema_version("2.0")
                .setCreated_by("TODO") // TODO
                ;
    }

    @Mapper
    public Phenopacket fromKey(Context<?> ctx, Phenopacket.Key key) {
        return new Phenopacket().setId(key);
    }

    private static final CurieManager.System SECTION_TYPE = new CurieManager.System("http://hl7.org/fhir/uv/phenomics-exchange/CodeSystem/section-type");
    @Mapper
    public List<Composition.SectionComponent> buildSections(Context<?> ctx, Set<Disease> diseases) {
        List<Composition.SectionComponent> result = new ArrayList<>();
        if(!diseases.isEmpty()) {

            Composition.SectionComponent diseasesSection = new Composition.SectionComponent();
            diseasesSection.setCode(new CodeableConcept().addCoding(new Coding().setCode("diseases").setSystem(SECTION_TYPE.system())));
            diseasesSection.setEntry(diseases.stream().map(referenceManager::getReference).toList());
            result.add(diseasesSection);
        }

        // TODO the others

        return result;
    }

    @Mapper
    public Enumerations.CompositionStatus loadStatus(Context<?> ctx, String status) {
        return new Enumerations.CompositionStatusEnumFactory().fromCode(status);
    }

    @Mapper("phenopacketType")
    public CodeableConcept getType(Context<?> ctx) {
        return new CodeableConcept().setText("Case report").addCoding(new Coding().setDisplay("Case report").setSystem("http://loinc.org").setCode("LP183503-4"));
    }

    @Mapper("phenopacketCreatedBy")
    public List<Reference> getCreatedBy(Context<?> ctx, String createdBy){
        return List.of(new Reference().setReferenceElement(new IdType("Organization", "TODO")));
    }

    @Mapper("phenopacketTitle")
    public String getTitle(Context<?> ctx, Phenopacket pp) {
        return "Phenopacket with id " + pp.getId().toString();
    }
}
