package es.upm.etsiinf.tfg.juanmahou.phenofhir.resolvers;

import es.upm.etsiinf.tfg.juanmahou.mapper.TypeRegistry;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.*;
import org.hl7.fhir.r4b.model.EnumFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

@Component
public class EnumFactoryResolver implements Resolver<BaseDataResolver.BaseDataContext> {
    private final ObjectProvider<TypeRegistry> typeRegistry;
    private final ObjectProvider<ObjectResolver> objectResolver;

    public EnumFactoryResolver(ObjectProvider<TypeRegistry> typeRegistry, ObjectProvider<ObjectResolver> objectResolver) {
        this.typeRegistry = typeRegistry;
        this.objectResolver = objectResolver;
    }

    @Override
    public String prefix() {
        return "enumFactory";
    }

    @Override
    public DataGetter resolve(Context<?> ctx, String dataPath, BaseDataResolver.BaseDataContext baseDataContext) {
        TypeRegistry typeRegistry = this.typeRegistry.getObject();
        try {
            String[] split = ResolverUtils.splitFirst(dataPath);
            ResolvableType factoryType = typeRegistry.resolve(split[0]);
            ResolvableType rt = factoryType.as(EnumFactory.class);
            if(rt.equalsType(ResolvableType.NONE)) throw new RuntimeException(factoryType + " does not implement EnumFactory");
            Class<?> c = factoryType.toClass();
            @SuppressWarnings("unchecked")
            Class<? extends EnumFactory<?>> enumFactory = (Class<? extends EnumFactory<?>>) c;
            Constructor<? extends EnumFactory<?>> constructor = enumFactory.getConstructor();
            EnumFactory<?> factory = constructor.newInstance();
            if(split.length == 2) {
                ObjectResolver objectResolver = this.objectResolver.getObject();
                return objectResolver.resolve(ctx, split[1], factory, factoryType);
            }else{
                return new DataGetter() {
                    @Override
                    public Object get() {
                        return factory;
                    }

                    @Override
                    public ResolvableType getType() {
                        return factoryType;
                    }
                };
            }
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException |
                 IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
