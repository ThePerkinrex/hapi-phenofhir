package es.upm.etsiinf.tfg.juanmahou.mapper.resolver.object;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.DataGetter;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ObjectResolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.Resolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ResolverUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class IndexResolver implements Resolver<ObjectResolver.ObjectResolverContext> {
    private static final Logger log = LoggerFactory.getLogger(IndexResolver.class);
    private final ObjectProvider<ObjectResolver> objectProvider;

    public IndexResolver(ObjectProvider<ObjectResolver> objectProvider) {
        this.objectProvider = objectProvider;
    }

    @Override
    public String prefix() {
        return "idx";
    }

    @Override
    public DataGetter resolve(Context<?> ctx, String dataPath, ObjectResolver.ObjectResolverContext parentContext) {
        if(parentContext.o() instanceof List<?> l) {
            ResolvableType elementType = parentContext.type().as(List.class).getGeneric(0);
            String[] split = ResolverUtils.splitFirst(dataPath);
            int idx = Integer.parseInt(split[0]);
            if(idx < 0 || idx >= l.size()) throw new IndexOutOfBoundsException(idx + " is not valid for " + l);
            Object o = l.get(idx);
            ObjectResolver resolver = objectProvider.getObject();
            if(split.length == 2)
                return resolver.resolve(ctx, split[1], o, elementType);
            else {
                return new DataGetter() {
                    @Override
                    public Object get() {
                        return o;
                    }

                    @Override
                    public ResolvableType getType() {
                        return elementType;
                    }
                };
            }
        }else{
            throw new RuntimeException("Cant index a " + parentContext.type());
        }

    }
}
