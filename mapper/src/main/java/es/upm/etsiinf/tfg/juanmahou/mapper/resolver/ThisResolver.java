package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.field.Getter;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ThisResolver implements Resolver {
    @Override
    public String prefix() {
        return "this";
    }

    @Override
    public DataGetter resolve(Context ctx, String dataPath) {
        List<Object> params = ctx.getParams();
        if(params.size()!=1) throw new RuntimeException("Unexpected context for resolving a this." + dataPath);
        Object o = params.getFirst();
        Getter<Object, Object> getter = new Getter<>(ResolvableType.forInstance(o), dataPath);
        return new DataGetter() {
            @Override
            public Object get() {
                return getter.get(o);
            }

            @Override
            public ResolvableType getType() {
                return getter.getFieldClass();
            }
        };
    }
}
