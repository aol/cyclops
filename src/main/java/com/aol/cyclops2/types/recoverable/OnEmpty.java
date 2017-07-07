package com.aol.cyclops2.types.recoverable;

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
     * @param supplier toNested determine new value for container
     * @return New Container with value if this is zero, otherwise this container
     */
    OnEmpty<T> onEmptyGet(Supplier<? extends T> supplier);

    /**
     * If this container instance is zero, throw the exception returned by the provided Supplier
     * 
     * @param supplier toNested create exception from
     * @return Throw exception if zero, otherwise this container
     */
    <X extends Throwable> OnEmpty<T> onEmptyThrow(Supplier<? extends X> supplier);

}
