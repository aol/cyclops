package com.oath.cyclops.types.recoverable;

import cyclops.control.Try;

import java.util.function.Supplier;

public interface OnEmptyError<T, E extends OnEmptyError<T,?>> {
    /**
     * If this container instance is zero, throw the exception returned by the provided Supplier
     *
     * @param supplier to create exception from
     * @return Throw exception if zero, otherwise this container
     */
    <X extends Throwable> Try<E, X> onEmptyTry(Supplier<? extends X> supplier);
}
