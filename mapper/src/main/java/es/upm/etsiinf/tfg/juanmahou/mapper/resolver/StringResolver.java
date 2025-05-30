package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

@Component
public class StringResolver implements Resolver<BaseDataResolver.BaseDataContext>{
    @Override
    public String prefix() {
        return "string";
    }

    @Override
    public DataGetter resolve(Context<?> ctx, String dataPath, BaseDataResolver.BaseDataContext baseDataContext) {
        return new DataGetter() {
            @Override
            public Object get() {
                return dataPath;
            }

            @Override
            public ResolvableType getType() {
                return ResolvableType.forClass(String.class);
            }
        };
    }
}
