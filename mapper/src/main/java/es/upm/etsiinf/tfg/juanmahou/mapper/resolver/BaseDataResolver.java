package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BaseDataResolver { // This one doesn't implement a prefix
    public record BaseDataContext(){}

    private final Map<String, Resolver<BaseDataContext>> resolvers;

    public BaseDataResolver(List<Resolver<BaseDataContext>> resolvers) {
        this.resolvers = resolvers.stream().collect(Collectors.toMap(Resolver::prefix, r -> r));
    }

    public DataGetter resolve(Context<?> ctx, String dataPath) {
        String[] parts = ResolverUtils.getPrefixWithDefault(dataPath, "this");
        Resolver<BaseDataContext> r = resolvers.get(parts[0]);
        if(r == null) throw new RuntimeException("Resolver for prefix " + parts[0] + " not found");
        return r.resolve(ctx, parts[1], new BaseDataContext());
    }
}
