package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.springframework.stereotype.Component;

@Component
public class ThisResolver implements Resolver<BaseDataResolver.BaseDataContext> {
    private final ObjectResolver objectResolver;

    public ThisResolver(ObjectResolver objectResolver) {
        this.objectResolver = objectResolver;
    }

    @Override
    public String prefix() {
        return "this";
    }

    @Override
    public DataGetter resolve(Context<?> ctx, String dataPath, BaseDataResolver.BaseDataContext parentContext) {
        if(ctx.getParams().size() != 1) throw new RuntimeException("Can't call this. resolver on more than one input object");
        if(dataPath.isEmpty()) {
            return new DataGetter() {
                @Override
                public Object get() {
                    return ctx.getParams().getFirst();
                }
            };
        }else{
            return objectResolver.resolve(ctx, dataPath, ctx.getParams().getFirst());
        }
    }
}
