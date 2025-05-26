package es.upm.etsiinf.tfg.juanmahou.mapper;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;

import java.util.List;

@FunctionalInterface
public interface MapperRunner {
    Object run(Context ctx, List<Object> params) throws Exception;
    default Object run(List<Object> params) throws Exception {
        return run(null, params);
    }
}
