package com.aol.cyclops2.types.recoverable;

import java.util.function.Supplier;

/**
 * Represents a container that may be empty for which we can switch a container with another value
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of element(s) stored in this container
 * @param <R>  Data type of element(s) stored in the container toNested be used if this container is empty
 */
public interface OnEmptySwitch<T, R> {
    /**
     * Switch toNested container created by provided Supplier, if current Container empty
     * 
     * <pre>
     * {@code 
     *     ListX.empty().onEmptySwitch(()->ListX.of(1));
     * } 
     * </pre>
     * 
     * @param supplier toNested create replacement container
     * @return Either this container or if empty, an alternative returned by the provided supplier
     */
    OnEmptySwitch<T, R> onEmptySwitch(Supplier<? extends R> supplier);
}
