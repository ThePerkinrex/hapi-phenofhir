package es.upm.etsiinf.tfg.juanmahou.mapper.resolver.context;

import es.upm.etsiinf.tfg.juanmahou.mapper.TypeRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.*;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Component
public class ResultingResolver implements Resolver<ContextResolver.Ctx> {
    private final TypeRegistry typeRegistry;
    private final ObjectProvider<ObjectResolver> objectProvider;

    public ResultingResolver(TypeRegistry typeRegistry, ObjectProvider<ObjectResolver> objectProvider) {
        this.typeRegistry = typeRegistry;
        this.objectProvider = objectProvider;
    }

    @Override
    public String prefix() {
        return "resulting";
    }

    @Override
    public DataGetter resolve(Context<?> ctx, String dataPath, ContextResolver.Ctx ctx2) {
        String[] split = ResolverUtils.splitFirst(dataPath);
        if(split.length != 2) throw new RuntimeException(dataPath + " should have included the resulting type");
        ResolvableType type = typeRegistry.resolve(split[0]);
        ObjectResolver resolver = objectProvider.getObject();
        Context<?> frame = ctx.getFrameResultingInType(type);
        return resolver.resolve(ctx, split[1], frame, ResolvableType.forClass(Context.class));
    }
}
