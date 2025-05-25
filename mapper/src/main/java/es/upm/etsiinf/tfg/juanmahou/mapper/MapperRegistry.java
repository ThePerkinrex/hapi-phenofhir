package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.annotation.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class MapperRegistry {
    private static final Logger log = LoggerFactory.getLogger(MapperRegistry.class);

    public record MapperKey(ResolvableType ret, List<ResolvableType> params) {
        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            MapperKey mapperKey = (MapperKey) o;
            return ret.equalsType(mapperKey.ret) && params().size() == mapperKey.params.size() && IntStream.iterate(0, i -> 1+i).limit(params().size()).allMatch(i -> params.get(i).equalsType(mapperKey.params.get(i)));
        }

        @Override
        public int hashCode() {
            return Objects.hash(ret.toString(), params.stream().map(ResolvableType::toString).collect(Collectors.joining()));
        }
    }

    private java.util.Map<MapperKey, Map<String, Function<List<Object>, Object>>> mappers;

    public MapperRegistry(List<MapperClass> mapperClasses) {
        this.mappers = new HashMap<>();
        for(var m : mapperClasses) {
            registerMappers(m);
        }
    }

    public void registerMappers(MapperClass mapperClass) {
        Class<?> c = mapperClass.getClass();
        for (Method m : c.getDeclaredMethods()) {
            Mapper map = m.getAnnotation(Mapper.class);
            if(map != null) {
                if(!m.canAccess(mapperClass)) {
                    log.warn("Can't access {}", m.getName());
                    continue;
                }
                List<ResolvableType> mapperParams = IntStream
                        .iterate(0, i -> i+1)
                        .limit(m.getParameterCount())
                        .mapToObj(i -> ResolvableType.forMethodParameter(m, i))
                        .toList();
                MapperKey key = new MapperKey(
                        ResolvableType.forMethodReturnType(m),
                        mapperParams
                );
                var named = mappers.computeIfAbsent(key, k -> new HashMap<>());
                if(named.containsKey(map.value())) {
                    throw new RuntimeException("A mapper for types " + key + " and name " + map.value() + " has already been defined");
                }
                log.info("Registering mapper {} ({}) - {}", key, key.hashCode(), map.value());
                named.put(map.value(), params -> {
                    if(params.size() == mapperParams.size()) {
                        for(int i = 0; i < params.size(); i++) {
                            var t = ResolvableType.forInstance(params.get(i));
                            var u = mapperParams.get(i);
                            if(!t.equals(u)) {
                                throw new IllegalArgumentException(t + " doesn't match " + u);
                            }
                        }
                        try {
                            return m.invoke(mapperClass, params.toArray());
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    throw new IllegalArgumentException("Number of arguments is incorrect");
                });
            }
        }
    }


    public Function<List<Object>, Object> getMapper(ResolvableType ret, List<ResolvableType> args) {
        return getMapper(ret, args, "default");
    }

    public Function<List<Object>, Object> getMapper(ResolvableType ret, List<ResolvableType> args, String name) {
        log.info("Getting {} ({}) - {}", new MapperKey(ret, args), new MapperKey(ret, args).hashCode(), name);
        return mappers.computeIfAbsent(new MapperKey(ret, args), k -> new HashMap<>()).get(name);
    }
}
