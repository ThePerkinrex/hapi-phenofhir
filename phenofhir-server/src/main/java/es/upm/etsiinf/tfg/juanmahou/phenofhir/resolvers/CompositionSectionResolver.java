package es.upm.etsiinf.tfg.juanmahou.phenofhir.resolvers;

import es.upm.etsiinf.tfg.juanmahou.mapper.context.Context;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.DataGetter;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ObjectResolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.Resolver;
import es.upm.etsiinf.tfg.juanmahou.mapper.resolver.ResolverUtils;
import es.upm.etsiinf.tfg.juanmahou.phenofhir.id.CurieManager;
import org.hl7.fhir.r4b.model.Composition;
import org.hl7.fhir.r4b.model.Reference;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.ResolvableType;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CompositionSectionResolver implements Resolver<ObjectResolver.ObjectResolverContext> {
    private final ObjectProvider<CurieManager> curieManager;
    private final ObjectProvider<ObjectResolver> objectProvider;

    public CompositionSectionResolver(ObjectProvider<CurieManager> curieManager, ObjectProvider<ObjectResolver> objectProvider) {
        this.curieManager = curieManager;
        this.objectProvider = objectProvider;
    }

    @Override
    public String prefix() {
        return "comp-sec";
    }

    @Override
    public DataGetter resolve(Context ctx, String dataPath, ObjectResolver.ObjectResolverContext objectResolverContext) {
        if(objectResolverContext.o() instanceof Composition comp) {
            String[] split = ResolverUtils.splitFirst(dataPath);
            String[] secCURIE = split[0].split(":", 2);
            if(secCURIE.length != 2) throw new RuntimeException("section must be identified with a CURIE split by a :");
            CurieManager.System sys = curieManager.getObject().getSystemForCurie(secCURIE[0]);
            for(var sec : comp.getSection()) {
                for(var coding : sec.getCode().getCoding()) {
                    if(sys.system().equals(coding.getSystem()) && secCURIE[1].equals(coding.getCode())) {
                        ObjectResolver resolver = objectProvider.getObject();
                        ResolvableType rt = ResolvableType.forClassWithGenerics(List.class, Reference.class);
                        if(split.length == 2) return resolver.resolve(ctx, split[1], sec.getEntry(), rt);
                        return new DataGetter() {
                            @Override
                            public Object get() {
                                return sec.getEntry();
                            }

                            @Override
                            public ResolvableType getType() {
                                return rt;
                            }
                        };
                    }
                }
            }
            return () -> null;
        }else{
            throw new RuntimeException("Can't apply the composition section resolver on a " + objectResolverContext.type() + " (" + objectResolverContext.o() + ")");
        }
    }
}
