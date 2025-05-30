package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class ContextResolver implements Resolver<BaseDataResolver.BaseDataContext>{
    public record Ctx(){}


    private final Map<String, Resolver<Ctx>> resolvers;

    public ContextResolver(List<Resolver<Ctx>> resolvers) {
        this.resolvers = resolvers.stream().collect(Collectors.toMap(Resolver::prefix, r -> r));
    }

    @Override
    public String prefix() {
        return "ctx";
    }

    @Override
    public DataGetter resolve(Context<?> ctx, String dataPath, BaseDataResolver.BaseDataContext baseDataContext) {
        String[] parts = ResolverUtils.getPrefixWithDefault(dataPath, null);
        if(parts[0] == null) throw new RuntimeException("No context resolver type specified");
        Resolver<Ctx> r = resolvers.get(parts[0]);
        if(r == null) throw new RuntimeException("Resolver for prefix " + parts[0] + " not found");
        return r.resolve(ctx, parts[1], new Ctx());
    }
}
