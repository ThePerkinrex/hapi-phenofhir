package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class BaseDataResolver { // This one doesn't implement a prefix
    private final Map<String, Resolver<BaseDataResolver>> resolvers;

    public BaseDataResolver(List<Resolver<BaseDataResolver>> resolvers) {
        this.resolvers = resolvers.stream().collect(Collectors.toMap(Resolver::prefix, r -> r));
    }

    public DataGetter resolve(Context ctx, String dataPath) {
        String[] parts = dataPath.split("\\.", 2);
        String prefix, path;
        if (parts.length == 2) {
            prefix = parts[0];
            path = parts[1];
        }else{
            prefix = "this";
            path = dataPath;
        }
        Resolver<BaseDataResolver> r = resolvers.get(prefix);
        if(r == null) throw new RuntimeException("Resolver for prefix " + prefix + " not found");
        return r.resolve(ctx, path);
    }
}
