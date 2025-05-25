package es.upm.etsiinf.tfg.juanmahou.phenofhir.id;

import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.types.TypeUtils;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

@Component
public class KeyUtils {
    public Type getKeyType(Type pheno) {
        Class<?> phenoCls = TypeUtils.toClass(pheno);
        for(Type iface : phenoCls.getGenericInterfaces()) {
            if(iface instanceof ParameterizedType pt && WithId.class.equals(pt.getRawType())) {
                return pt.getActualTypeArguments()[0];
            }
        }
        throw new RuntimeException("This type does not implement WithId");
    }
}
