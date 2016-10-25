package com.aol.cyclops.types;

import java.util.function.Function;

/**
 * An interface that represents a Functor that operates on a single Value
 * The type of Functor is convertable between types that implement Value
 * 
 * @author johnmcclean
 *
 * @param <T> The type of value stored in this functor
 */
public interface ConvertableFunctor<T> extends Value<T>, Functor<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#map(java.util.function.Function)
     */
    @Override
    <R> ConvertableFunctor<R> map(Function<? super T, ? extends R> fn);
}
