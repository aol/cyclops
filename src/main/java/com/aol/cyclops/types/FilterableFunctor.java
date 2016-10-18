package com.aol.cyclops.types;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a Functor that is also Filterable (e.g. a Stream or Optional type)
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of the element(s) in this FilterableFunctor
 */
public interface FilterableFunctor<T> extends Filterable<T>, Functor<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Filterable#filter(java.util.function.Predicate)
     */
    FilterableFunctor<T> filter(Predicate<? super T> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Functor#map(java.util.function.Function)
     */
    <R> FilterableFunctor<R> map(Function<? super T, ? extends R> fn);

}
