package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ObjectResolver { // This one doesn't implement a prefix
    public record ObjectResolverContext(Object o, ResolvableType type) {}

    private final Map<String, Resolver<ObjectResolverContext>> resolvers;

    public ObjectResolver(List<Resolver<ObjectResolverContext>> resolvers) {
        this.resolvers = resolvers.stream().collect(Collectors.toMap(Resolver::prefix, r -> r));
    }

    public DataGetter resolve(Context<?> ctx, String dataPath, Object o) {
        return resolve(ctx, dataPath, o, ResolvableType.forInstance(o));
    }

    public DataGetter resolve(Context<?> ctx, String dataPath, Object o, ResolvableType type) {
        String[] parts = ResolverUtils.getPrefixWithDefault(dataPath, "field");

        Resolver<ObjectResolverContext> r = resolvers.get(parts[0]);
        if(r == null) throw new RuntimeException("Resolver for prefix " + parts[0] + " not found");
        return r.resolve(ctx, parts[1], new ObjectResolverContext(o, type));
    }
}
