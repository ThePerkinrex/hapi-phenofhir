package es.upm.etsiinf.tfg.juanmahou.mapper.resolver;

import org.springframework.core.ResolvableType;

@FunctionalInterface
public interface DataGetter {
    Object get();
    default ResolvableType getType() {
        return ResolvableType.forInstance(get());
    }
}
