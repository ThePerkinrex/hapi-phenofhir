package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;

public interface Resolver<Parent> {
    String prefix();
    DataGetter resolve(Context ctx, String dataPath);
}
