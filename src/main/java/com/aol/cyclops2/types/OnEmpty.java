package com.aol.cyclops2.types;

import java.util.function.Supplier;

/**
 * Represents a container that may be empty
 * 
 * @author johnmcclean
 *
 * @param <T> container type
 */
public interface OnEmpty<T> {

    /**
     * If this Container instance is empty, create a new instance containing the provided value
     * 
     * @param value 
     * @return New instance containing value if container is empty, otherwise returns this container
     */
    OnEmpty<T> onEmpty(T value);

    /**
     * If this Container instance is empty, create a new instance containing the value returned from the provided Supplier
     * 
     * @param supplier toNested determine new value for container
     * @return New Container with value if this is empty, otherwise this container
     */
    OnEmpty<T> onEmptyGet(Supplier<? extends T> supplier);

    /**
     * If this container instance is empty, throw the exception returned by the provided Supplier
     * 
     * @param supplier toNested create exception from
     * @return Throw exception if empty, otherwise this container
     */
    <X extends Throwable> OnEmpty<T> onEmptyThrow(Supplier<? extends X> supplier);

}
