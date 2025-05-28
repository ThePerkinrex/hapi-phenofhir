package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.*;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.context.ResultingResolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.object.FieldResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;

import java.util.List;

@TestConfiguration
public class TestConfig {
    private static final Logger log = LoggerFactory.getLogger(TestConfig.class);

    @Bean
    public TypeRegistry typeRegistry() {
        return new TypeRegistry();
    }

    @Bean
    public BaseDataResolver baseDataResolver(List<Resolver<BaseDataResolver.BaseDataContext>> resolvers) {
        log.info("Loading configured baseDataResolver");
        return new BaseDataResolver(resolvers);
    }

    @Bean
    public FieldResolver fieldResolver(ObjectProvider<ObjectResolver> provider) {
        return new FieldResolver(provider);
    }

    @Bean
    public ObjectResolver objectResolver(List<Resolver<ObjectResolver.ObjectResolverContext>> provider) {
        return new ObjectResolver(provider);
    }

    @Bean
    public ThisResolver thisResolver(ObjectResolver provider) {
        return new ThisResolver(provider);
    }

    @Bean
    public ResultingResolver resultingResolver(TypeRegistry typeRegistry,ObjectProvider<ObjectResolver> provider) {
        return new ResultingResolver(typeRegistry, provider);
    }

    @Bean
    public ContextResolver contextResolver(List<Resolver<ContextResolver.Ctx>> resolvers) {
        return new ContextResolver(resolvers);
    }
}
