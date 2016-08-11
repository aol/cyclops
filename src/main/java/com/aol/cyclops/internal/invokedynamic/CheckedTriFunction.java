package com.aol.cyclops.internal.invokedynamic;

public interface CheckedTriFunction<T1, T2, T3, R> {

    public R apply(T1 t1, T2 t2, T3 t3) throws Throwable;

}
