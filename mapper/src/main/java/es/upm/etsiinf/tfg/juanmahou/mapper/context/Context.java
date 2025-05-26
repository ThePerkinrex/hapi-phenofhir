package es.upm.etsiinf.tfg.juanmahou.mapper.context;

import es.upm.etsiinf.tfg.juanmahou.entities.id.Id;
import org.springframework.core.ResolvableType;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;

public class Context {
    private final Context previous;
    private final List<Object> params;
    private final ResolvableType result;
    private Object id = null;

    public Context(List<Object> params, ResolvableType result) {
        this.result = result;
        this.previous = null;
        this.params = params;
    }

    private Context(Context previous, List<Object> params, ResolvableType result) {
        this.previous = previous;
        this.params = params;
        this.result = result;
    }

    public Context next(List<Object> params, ResolvableType result) {
        return new Context(this, params, result);
    }

    public Object getId() {
        return id;
    }

    public void setId(Id id) {
        this.id = id;
    }

    public List<Object> getParams() {
        return params;
    }

    public ResolvableType getResult() {
        return result;
    }

    public Context getPrevious() {
        return previous;
    }

    private Stream<Context> stackStream() {
        return Stream.iterate(this, c -> c.getPrevious() != null, Context::getPrevious);
    }

    public Object getForType(ResolvableType type) {
        for (Iterator<Context> it = stackStream().iterator(); it.hasNext(); ) {
            Context ctx = it.next();
            for(Object o : ctx.params) {
                if(ResolvableType.forInstance(o).equalsType(type)) {
                    return o;
                }
            }

        }

        return null;
    }

    public Context getFrameResultingInType(ResolvableType type) {
        for (Iterator<Context> it = stackStream().iterator(); it.hasNext(); ) {
            Context ctx = it.next();
            if(ctx.result.equalsType(type)) {
                return ctx;
            }
        }
        return null;
    }
}
