package es.upm.etsiinf.tfg.juanmahou.mapper.resolver.object;

import es.upm.etsiinf.tfg.juanmahou.mapper.TypeRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.field.Getter;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.DataGetter;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ObjectResolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.Resolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ResolverUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Component
public class CastResolver implements Resolver<ObjectResolver.ObjectResolverContext> {
    private static final Logger log = LoggerFactory.getLogger(CastResolver.class);
    private final ObjectProvider<ObjectResolver> objectProvider;
    private final TypeRegistry typeRegistry;

    public CastResolver(ObjectProvider<ObjectResolver> objectProvider, TypeRegistry typeRegistry) {
        this.objectProvider = objectProvider;
        this.typeRegistry = typeRegistry;
    }

    @Override
    public String prefix() {
        return "as";
    }

    @Override
    public DataGetter resolve(Context ctx, String dataPath, ObjectResolver.ObjectResolverContext parentContext) {
        String[] split = ResolverUtils.splitFirst(dataPath);
        ResolvableType type = typeRegistry.resolve(split[0]);
        ObjectResolver resolver = objectProvider.getObject();
        Object o = parentContext.o();
        ResolvableType pt = parentContext.type();
        if(ResolvableType.forClass(Object.class).equalsType(pt)) {
            pt = ResolvableType.forInstance(o);
        }
        ResolvableType rt = pt.as(type.toClass());
        if(rt.equalsType(ResolvableType.NONE)) throw new RuntimeException("Can't cast " + o + " (" + pt + ") to " + type);
        if(split.length == 2)
            return resolver.resolve(ctx, split[1], o, type);
        else {
            return new DataGetter() {
                @Override
                public Object get() {
                    return o;
                }

                @Override
                public ResolvableType getType() {
                    return rt;
                }
            };
        }
    }
}
