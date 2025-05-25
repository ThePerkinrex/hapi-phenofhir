package es.upm.etsiinf.tfg.juanmahou.phenofhir.generator;

import entities.org.phenopackets.schema.v2.core.MetaData;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.Generator;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.GeneratorContext;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.IGenerator;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.mappers.PhenoMapper;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.persistence.RepositoryProvider;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.registry.MapperRegistry;
import org.hl7.fhir.r4b.model.Composition;
import org.hl7.fhir.r4b.model.Reference;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Component;

@Generator("MetaData")
@Component
public class MetaDataGenerator implements IGenerator<MetaData, MetaDataGenerator.MetaDataGeneratorConfig> {

    public static class MetaDataGeneratorConfig {

    }
    private final ObjectProvider<MapperRegistry> mapperRegistry;
    private final CrudRepository<MetaData, MetaData.Key> repository;

    public MetaDataGenerator(ObjectProvider<MapperRegistry> mapperRegistry, RepositoryProvider repositoryProvider) {
        this.mapperRegistry = mapperRegistry;
        this.repository = repositoryProvider.getCrudRepository(MetaData.class);
    }

    @Override
    public Class<MetaDataGeneratorConfig> getConfigClass() {
        return MetaDataGeneratorConfig.class;
    }

    @Override
    public Class<MetaData> getTargetClass() {
        return MetaData.class;
    }

    @Override
    public MetaData generate(MetaDataGeneratorConfig config, GeneratorContext ctx) {
        if(ctx.getCurrentResource() instanceof Composition comp) {
            MetaData result = new MetaData()
                    .setCreated(comp.getDate().toInstant())
                    .setCreated_by(comp.getAuthorFirstRep().getReference())
                    .setPhenopacket_schema_version("2.0");
            // TODO resources

            return repository.save(result);
        }else {
            throw new RuntimeException("Unexpected resource " + ctx.getCurrentResource());
        }
    }
}
