package com.aol.cyclops;

import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

/**
 * An (associative) binary operation for combining values.
 * Implementations should obey associativity laws.
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements to be combined
 */
@FunctionalInterface
public interface Semigroup<T> extends BinaryOperator<T> {
    
    /* (non-Javadoc)
     * @see java.util.function.BiFunction#apply(java.lang.Object, java.lang.Object)
     */
    @Override
    T apply(T t, T u);
   

}
