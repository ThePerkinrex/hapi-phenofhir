package es.upm.etsiinf.tfg.juanmahou.phenofhir.id;

import es.upm.etsiinf.tfg.juanmahou.entities.id.WithId;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;


@Component
public class KeyUtils {
    public ResolvableType getKeyType(ResolvableType pheno) {
        ResolvableType withId = pheno.as(WithId.class);
        if(!ResolvableType.NONE.equalsType(withId)) {
            return withId.getGeneric(0);
        }
        throw new RuntimeException("This type does not implement WithId");
    }
}
