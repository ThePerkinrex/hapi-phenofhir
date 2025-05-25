package es.upm.etsiinf.tfg.juanmahou.phenofhir.id;

import org.hl7.fhir.r4b.model.Identifier;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class IdManager {
    private final CurieManager curieManager;


    public IdManager(CurieManager curieManager) {
        this.curieManager = curieManager;
    }

    public String idAsCurie(Identifier identifier) {
        String curie = curieManager.getCurieForSystem(new CurieManager.System(identifier.getSystem()));
        return curie + ":" + identifier.getValue();
    }

    public Identifier curieAsId(String curie) {
        String[] parts = curie.split(":", 2);
        if (parts.length != 2) throw new RuntimeException(curie + " is not a valid CURIE");
        CurieManager.System system = curieManager.getSystemForCurie(parts[0]);
        return new Identifier().setSystem(system.system()).setValue(parts[1]);
    }

    private String getNewValue() {
        return UUID.randomUUID().toString().toLowerCase();
    }

    public Identifier getNewIdentifier() {
        var own = curieManager.getOwnSystem();
        return new Identifier().setSystem(own.system()).setValue(getNewValue());
    }

    public String getNewCurie() {
        var own = curieManager.getOwnCurie();
        return own + ":" + getNewValue();
    }
}
