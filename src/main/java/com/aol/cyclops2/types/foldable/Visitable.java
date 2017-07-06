package com.aol.cyclops2.types.foldable;

import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Data type that may contain a singleUnsafe visitable element
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element stored in this Visitable
 */
public interface Visitable<T> {

    /**
     * The provided function is executed with the value stored in this Visitable if replaceWith is present,
     * otherwise the provided Supplier is executed instead.
     * 
     * @param present Function to execute if this Visitable has a value
     * @param absent Supplier to execute if this Visitable does not have a Value
     * @return Result of the executed Function or Supplier
     */
    <R> R visit(Function<? super T, ? extends R> present, Supplier<? extends R> absent);
}
