package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.adapter.MapperResultAdapter;
import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.Mapping;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.condition.SourceCondition;
import es.upm.etsiinf.tfg.juanmahou.mapper.config.field.Field;
import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.field.Setter;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.BaseDataResolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.DataGetter;
import es.upm.etsiinf.tfg.juanmahou.mapper.util.PrimitiveUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Component
public class MapperRegistry {
    private static final Logger log = LoggerFactory.getLogger(MapperRegistry.class);

    public record MapperKey(ResolvableType ret, List<ResolvableType> params) {
        public boolean paramsEquals(List<ResolvableType> other) {
            return params().size() == other.size() && IntStream.iterate(0
                    , i -> 1 + i).limit(params().size()).allMatch(i -> params.get(i).equalsType(other.get(i)));
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MapperKey mapperKey = (MapperKey) o;
            return ret.equalsType(mapperKey.ret) && paramsEquals(mapperKey.params());
        }

        @Override
        public int hashCode() {
            return Objects.hash(ret.toString(),
                    params.stream().map(ResolvableType::toString).collect(Collectors.joining()));
        }
    }

    private java.util.Map<MapperKey, Map<String, MapperRunner>> mappers;
    private final TypeRegistry typeRegistry;
    private final BaseDataResolver baseDataResolver;
    private final List<MapperResultAdapter> resultAdapters;

    public MapperRegistry(List<MapperClass> mapperClasses, TypeRegistry typeRegistry,
                          BaseDataResolver baseDataResolver, List<MapperResultAdapter> resultAdapters) {
        this.typeRegistry = typeRegistry;
        this.baseDataResolver = baseDataResolver;
        this.resultAdapters = resultAdapters;
        this.mappers = new HashMap<>();
        for (var m : mapperClasses) {
            registerMappers(m);
        }
    }

    private void registerRunner(MapperKey key, String name, MapperRunner runner) {
        var named = mappers.computeIfAbsent(key, k -> new HashMap<>());
        if (named.containsKey(name)) {
            throw new RuntimeException("A mapper for types " + key + " and name " + name + " has " +
                    "already been defined");
        }
        log.info("Registering mapper {} ({}) - {}", key, key.hashCode(), name);
        named.put(name, (ctx, params, onSet, onBuilt) -> {
            log.info("Running mapper {} - {} with params {} ({})", key, name, params, ResolvableType.forInstance(params));
            runner.run(ctx, params, res -> {
                for(var adapter : resultAdapters) {
                    res = adapter.adaptOnSet(res);
                    ResolvableType rt = ResolvableType.forInstance(res);
                    if(res != null && !key.ret.isAssignableFrom(rt) && !key.ret.toClass().isAssignableFrom(rt.toClass())) {
                        log.error("res is not null and {} is not assignable from {}", key.ret, rt);
                        throw new RuntimeException();
                    }
                }
                onSet.accept(res);
            }, res -> {
                for(var adapter : resultAdapters) {
                    res = adapter.adaptOnBuilt(res);

                    ResolvableType rt = ResolvableType.forInstance(res);
                    if(res != null && !key.ret.isAssignableFrom(rt) && !key.ret.toClass().isAssignableFrom(rt.toClass())) {
                        log.error("res is not null and {} is not assignable from {}", key.ret, rt);
                        throw new RuntimeException();
                    }
                }
                onBuilt.accept(res);

            });

        });
    }

    public void registerMappers(MapperClass mapperClass) {
        Class<?> c = mapperClass.getClass();
        for (Method m : c.getDeclaredMethods()) {
            Mapper map = m.getAnnotation(Mapper.class);
            if (map != null) {
                if (!m.canAccess(mapperClass)) {
                    log.warn("Can't access {}", m);
                    continue;
                }
                if (m.getParameterCount() == 0) {
                    log.warn("Mapper {} doesn't receive any arguments (needs to receive context), ignoring", m);
                    continue;
                }
                boolean setAfterMethod;
                ResolvableType result;
                ResolvableType retType = ResolvableType.forMethodReturnType(m);
                ResolvableType context = ResolvableType.forMethodParameter(m, 0).as(Context.class);
                if (context.equalsType(ResolvableType.NONE)) {
                    log.warn("Mapper {} doesn't receive the context as first argument, ignoring", m);
                    continue;
                }
                if(ResolvableType.forClass(Void.class).isAssignableFrom(retType)) {
                    setAfterMethod = false;
                    result = context.getGeneric(0);
                }else{
                    result = PrimitiveUtil.wrapPrimitive(retType);
                    setAfterMethod = true;
                }
                List<ResolvableType> mapperParams = IntStream
                        .iterate(1, i -> i + 1)
                        .limit(m.getParameterCount() - 1)
                        .mapToObj(i -> ResolvableType.forMethodParameter(m, i))
                        .map(PrimitiveUtil::wrapPrimitive)
                        .toList();
                MapperKey key = new MapperKey(
                        result,
                        mapperParams
                );
                MapperRunner runner = (parent, params, onSet, onBuilt) -> {
                    if (params.size() == mapperParams.size()) {
                        Object[] nextParams = new Object[params.size() + 1];
                        log.info("Running mapper {} - {}", key, map.value());
                        for (int i = 0; i < params.size(); i++) {
                            Object p = params.get(i);
                            var t = ResolvableType.forInstance(p);
                            var u = mapperParams.get(i);
                            if (p != null && !u.isAssignableFrom(t) && !u.toClass().isAssignableFrom(t.toClass())) {
                                throw new IllegalArgumentException(t + " is not assignable to " + u);
                            }
                            nextParams[i + 1] = p;
                        }
                        Context<Object> ctx = parent == null ? new Context<>(params, result) : parent.next(params, result);
                        ctx.onSet(onSet);
                        ctx.onSet(onBuilt);
                        nextParams[0] = ctx;

                        try {
                            Object res = m.invoke(mapperClass, nextParams);
                            log.info("Invoked");
                            if(setAfterMethod) {
                                ctx.accept(res);
                                log.info("Set after method");
                            }
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }else{
                        throw new IllegalArgumentException("Number of arguments is incorrect");
                    }
                };
                registerRunner(key, map.value(), runner);
            }
        }
    }

    private record MappingField(Setter<Object, Object> setter, Field field) {
    }

    public void registerMapping(Mapping mapping) throws NoSuchMethodException {
        final String NAME = mapping.getName();
        ResolvableType source = typeRegistry.resolve(mapping.getSource());
        ResolvableType target = typeRegistry.resolve(mapping.getTarget());
        Constructor<?> targetConstructor = Objects.requireNonNull(target.resolve()).getConstructor();
        List<MappingField> fields = mapping.getFields().entrySet().stream().map(
                e -> new MappingField(
                        new Setter<>(target, e.getKey()),
                        e.getValue())
        ).toList();
        List<SourceCondition> conditions = mapping.getSourceConditions();
        MapperRunner runner = (parent, params, onSet, onBuilt) -> {
            if (params.size() != 1 || !ResolvableType.forInstance(params.getFirst()).equalsType(source))
                throw new RuntimeException("params dont match the expected source " + source);
            Object o = params.getFirst();
            Object res;
            Context<Object> ctx = parent == null ? new Context<>(params, target) : parent.next(params, target);
            ctx.onSet(onSet);
            try {
                res = targetConstructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new RuntimeException(e);
            }
            for (SourceCondition condition : conditions) {
                if(!condition.check(o)) {
                    throw condition.getException(o);
                }
            }
            ctx.accept(res);

            List<MappingField> rest = new ArrayList<>(fields.size());
            Consumer<Object> fieldOnBuilt = new Consumer<Object>() {
                private int i = 0;
                @Override
                public void accept(Object o) {
                    if(++i >= fields.size()) {
                        onBuilt.accept(res);
                    }
                }
            };

            for (var f : fields) {
                if(f.field.isId()) {
                    Consumer<Object> setter =result -> {
                        f.setter.set(res, result);

                        ctx.setId(result);
                    };
                    processField(f, ctx, setter, fieldOnBuilt);

                }else{
                    rest.add(f);
                }
            }
            for(var f : rest) {
                Consumer<Object> setter =result -> {
                    f.setter.set(res, result);
                };

                processField(f, ctx, setter, fieldOnBuilt);

            }
        };
        MapperKey key = new MapperKey(
                target,
                List.of(source)
        );
        registerRunner(key, NAME, runner);
    }

    private void processField(MappingField f, Context<Object> ctx, Consumer<Object> onSet, Consumer<Object> onBuilt) throws Exception {
        List<DataGetter> dataGetters =
                f.field.getSources().stream().map(s -> baseDataResolver.resolve(ctx, s)).toList();
        MapperRunner fieldMapper = getMapper(f.setter.getFieldClass(),
                dataGetters.stream().map(DataGetter::getType).toList(), f.field.getMapper());
        if(fieldMapper == null) throw new NullPointerException("Cant map fields " + f + " types " + dataGetters.stream().map(DataGetter::getType).toList());
        fieldMapper.run(ctx, dataGetters.stream().map(DataGetter::get).toList(), onSet, onBuilt);
    }


    public MapperRunner getMapper(ResolvableType ret, List<ResolvableType> args) {
        return getMapper(ret, args, "default");
    }

    public MapperRunner getMapper(ResolvableType ret, List<ResolvableType> args, String name) {
        log.info("Getting {} ({}) - {}", new MapperKey(ret, args), new MapperKey(ret, args).hashCode(), name);
        MapperRunner res = mappers.computeIfAbsent(new MapperKey(ret, args), k -> new HashMap<>()).get(name);
        if(res == null && args.size() == 1 && ret.isAssignableFrom(args.getFirst())) {
            res = ((ctx, params, onSet, onBuilt) -> {
                Object x = params.getFirst();
                Object onSetObj = x;
                ResolvableType original = ResolvableType.forInstance(x);
                for(var adapter : resultAdapters) {
                    onSetObj = adapter.adaptOnSet(onSetObj);
                    assert original.isAssignableFrom(ResolvableType.forInstance(onSetObj));
                    x = adapter.adaptOnBuilt(x);
                    assert original.isAssignableFrom(ResolvableType.forInstance(x));
                }
                onSet.accept(onSetObj);
                onBuilt.accept(x);
            });
        }
        return res;
    }

    public record MapperAndData(MapperRunner runner, MapperKey key, String name) {}

    public Stream<MapperAndData> getAllForArgs(List<ResolvableType> args) {
        return mappers
                .entrySet()
                .stream()
                .filter(e -> e.getKey().paramsEquals(args))
                .flatMap(e -> e
                        .getValue()
                        .entrySet()
                        .stream()
                        .map(x -> new MapperAndData(x.getValue(), e.getKey(), x.getKey())))
                ;
    }



    public Stream<MapperAndData> getAllForRet(ResolvableType ret) {
        return mappers
                .entrySet()
                .stream()
                .filter(e -> ret.isAssignableFrom(e.getKey().ret()))
                .flatMap(e -> e
                        .getValue()
                        .entrySet()
                        .stream()
                        .map(x -> new MapperAndData(x.getValue(), e.getKey(), x.getKey())))
                ;
    }
}
