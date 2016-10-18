package com.aol.cyclops.types;

import java.util.function.Function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.types.anyM.AnyMValue;

/**
 * A type that represents a Monad that wraps a single value
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element stored inside this Monad
 */
public interface MonadicValue<T> extends Value<T>, Unit<T>, Functor<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    public <T> MonadicValue<T> unit(T unit);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#map(java.util.function.Function)
     */
    <R> MonadicValue<R> map(Function<? super T, ? extends R> fn);

    /**
     * @return This monad wrapped as an AnyMValue
     */
    default AnyMValue<T> anyM() {
        return AnyM.ofValue(this);
    }

    /**
     * Perform a coflatMap operation. The mapping function accepts this MonadicValue and returns
     * a single value to be wrapped inside a Monad.
     * 
     * <pre>
     * {@code 
     *   Maybe.none().coflatMap(m -> m.isPresent() ? m.get() : 10);
     *   //Maybe[10]
     * }
     * </pre>
     * 
     * @param mapper Mapping / transformation function
     * @return MonadicValue wrapping return value from transformation function applied to the value inside this MonadicValue
     */
    default <R> MonadicValue<R> coflatMap(Function<? super MonadicValue<T>, R> mapper) {
        return mapper.andThen(r -> unit(r))
                     .apply(this);
    }

    //cojoin
    /**
     * cojoin pattern. Nests this Monad inside another.
     * 
     * @return Nested Monad
     */
    default MonadicValue<MonadicValue<T>> nest() {
        return this.map(t -> unit(t));
    }

}
