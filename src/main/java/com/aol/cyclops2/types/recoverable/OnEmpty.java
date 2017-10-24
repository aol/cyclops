package com.aol.cyclops2.types.recoverable;

import cyclops.control.Try;

import java.util.function.Supplier;

/**
 * Represents a container that may be zero
 * 
 * @author johnmcclean
 *
 * @param <T> container type
 */
public interface OnEmpty<T> {

    /**
     * If this Container instance is zero, create a new instance containing the provided value
     * 
     * @param value 
     * @return New instance containing value if container is zero, otherwise returns this container
     */
    OnEmpty<T> onEmpty(T value);

    /**
     * If this Container instance is zero, create a new instance containing the value returned from the provided Supplier
     * 
     * @param supplier to determine new value for container
     * @return New Container with value if this is zero, otherwise this container
     */
    OnEmpty<T> onEmptyGet(Supplier<? extends T> supplier);



}
