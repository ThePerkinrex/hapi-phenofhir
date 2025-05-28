package es.upm.etsiinf.tfg.juanmahou.mapper.context;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ResolvableType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Context<T> implements Consumer<T>, Supplier<T> {
    private static final Logger log = LoggerFactory.getLogger(Context.class);
    private final Context<?> previous;
    private final List<Object> params;
    private final ResolvableType result;
    private T resultInstance = null;
    private List<Consumer<T>> onSet = new ArrayList<>();
    private Object id = null;

    public Context(List<Object> params, ResolvableType result) {
        this(null, params, result);
    }

    private Context(Context<?> previous, List<Object> params, ResolvableType result) {
        this.previous = previous;
        this.params = params;
        this.result = result;
        log.info("Entered ctx");
        int i = 0;
        for (Iterator<Context<?>> it = stackStream().iterator(); it.hasNext(); ) {
            Context<?> ctx = it.next();
            log.info("STACK {} - {}", i++, ctx);
        }
    }

    public <U> Context<U> next(List<Object> params, ResolvableType result) {
        return new Context<>(this, params, result);
    }

    public Context<T> adapt(Function<T, T> f) {
        Context<T> n = new Context<>(this.previous, this.params, this.result);
        n.onSet(x -> this.accept(f.apply(x)));
        return n;
    }

    public Object getId() {
        log.info("Getting id {}", id);
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    public List<Object> getParams() {
        return params;
    }

    public ResolvableType getResult() {
        return result;
    }

    public Context<?> getPrevious() {
        return previous;
    }

    private Stream<Context<?>> stackStream() {
        return Stream.iterate(this, Objects::nonNull, Context::getPrevious);
    }

    public Object getForParamType(ResolvableType type) {
        for (Iterator<Context<?>> it = stackStream().iterator(); it.hasNext(); ) {
            Context<?> ctx = it.next();
            for(Object o : ctx.params) {
                if(ResolvableType.forInstance(o).equalsType(type)) {
                    return o;
                }
            }
        }
        return null;
    }

    public Context<?> getFrameResultingInType(ResolvableType type) {
        for (Iterator<Context<?>> it = stackStream().iterator(); it.hasNext(); ) {
            Context<?> ctx = it.next();
            if(ctx.result.equalsType(type)) {
                return ctx;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return "Context{" +
                "params=" + params +
                ", result=" + result +
                ", id=" + id +
                '}';
    }

    public void onSet(Consumer<T> onSet) {
        if(this.resultInstance!=null) {
            onSet.accept(this.resultInstance);
        }
        this.onSet.add(onSet);
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param o the input argument
     */
    @Override
    public void accept(T o) {
        this.resultInstance = o;
        for(var c : onSet) {
            c.accept(o);
        }
    }

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    public T get() {
        return this.resultInstance;
    }
}
