package es.upm.etsiinf.tfg.juanmahou.phenofhir.generator;

import es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.Generator;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.GeneratorContext;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.generator.registry.IGenerator;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.IdManager;
import jakarta.validation.constraints.NotEmpty;
import org.hl7.fhir.r4b.model.Identifier;
import org.hl7.fhir.r4b.model.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

@Generator("FillIfMissing")
@Component
public class FillIfMissing implements IGenerator<String, FillIfMissing.Config> {
    private final ObjectProvider<es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config> config;
    private final ObjectProvider<IdManager> idManager;

    public FillIfMissing(ObjectProvider<es.upm.etsiinf.tfg.juanmahou.phenofhir.config.Config> config, ObjectProvider<IdManager> idManager) {
        this.config = config;
        this.idManager = idManager;
    }

    public static class Config {
    }

    @Override
    public Class<Config> getConfigClass() {
        return Config.class;
    }

    @Override
    public Class<String> getTargetClass() {
        return String.class;
    }

    @Override
    public String generate(Config _conf, GeneratorContext context) {
        var config = this.config.getObject();
        var ownIdentifiers = config.getOwnIdentifiers();
        var idManager = this.idManager.getObject();
        Resource res = context.getCurrentResource();
        if(res == null) throw new RuntimeException("No resource set");
        try {
            Method m = res.getClass().getMethod("getIdentifier");
            if (List.class.isAssignableFrom(m.getReturnType())) {
                List<Object> identifiers = (List<Object>) m.invoke(res);
                for(Object o : identifiers) {
                    if (o instanceof Identifier id && ownIdentifiers.getSystem().equals(id.getSystem())) {
                        return idManager.idAsCurie(id);
                    }
                }
            }
        } catch (NoSuchMethodException | InvocationTargetException| IllegalAccessException ignored) {

        }
        return idManager.getNewCurie();
    }
}
