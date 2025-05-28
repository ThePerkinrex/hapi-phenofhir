package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

@FunctionalInterface
public interface MapperRunner {
    void run(Context<?> parent, List<Object> params, Consumer<Object> onSet, Consumer<Object> onBuilt) throws Exception;
    default void run(List<Object> params, Consumer<Object> onSet, Consumer<Object> onBuilt) throws Exception {
        run(null, params, onSet, onBuilt);
    }

    default void run(List<Object> params, Consumer<Object> onBuilt) throws Exception {
        run(params, x -> {}, onBuilt);
    }
}
