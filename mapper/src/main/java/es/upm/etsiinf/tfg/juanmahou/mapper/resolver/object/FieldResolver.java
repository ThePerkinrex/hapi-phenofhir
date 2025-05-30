package es.upm.etsiinf.tfg.juanmahou.mapper.resolver.object;

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
public class FieldResolver implements Resolver<ObjectResolver.ObjectResolverContext> {
    private static final Logger log = LoggerFactory.getLogger(FieldResolver.class);
    private final ObjectProvider<ObjectResolver> objectResolverProvider;

    public FieldResolver(ObjectProvider<ObjectResolver> objectResolverProvider) {
        this.objectResolverProvider = objectResolverProvider;
    }

    @Override
    public String prefix() {
        return "field";
    }

    @Override
    public DataGetter resolve(Context<?> ctx, String dataPath, ObjectResolver.ObjectResolverContext parentContext) {
        String[] split = ResolverUtils.splitFirst(dataPath);
        Object o = parentContext.o();
        Getter<Object, Object> getter = new Getter<>(parentContext.type(), split[0]);
        if(split.length == 2) {
            Object child = getter.get(o);
            if(child == null) throw new NullPointerException(dataPath + " resolved a child which was null");
            ObjectResolver resolver = objectResolverProvider.getObject();
            log.info("Resolving {} for child {}", split[1], child);
            return resolver.resolve(ctx, split[1], child, getter.getFieldClass()); // Nesting
        }
        return new DataGetter() {
            @Override
            public Object get() {
                return getter.get(o);
            }

            @Override
            public ResolvableType getType() {
                ResolvableType rt = getter.getFieldClass();
                if(rt.equalsType(ResolvableType.forClass(Object.class))) { // Try to upcast if too generic
                    return ResolvableType.forInstance(get());
                }
                return rt;
            }
        };
    }
}
