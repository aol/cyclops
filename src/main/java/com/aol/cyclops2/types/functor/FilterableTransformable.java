package com.aol.cyclops2.types.functor;

import com.aol.cyclops2.types.Filters;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a Transformable that is also Filters (e.g. a Stream or Optional type)
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of the element(s) in this FilterableTransformable
 */
public interface FilterableTransformable<T> extends Filters<T>, Transformable<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.Filters#filter(java.util.function.Predicate)
     */
    @Override
    FilterableTransformable<T> filter(Predicate<? super T> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops2.types.functor.Transformable#transform(java.util.function.Function)
     */
    @Override
    <R> FilterableTransformable<R> map(Function<? super T, ? extends R> fn);

}
