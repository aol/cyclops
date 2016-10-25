package com.aol.cyclops.types;

public interface FlatMap<T> extends Functor<T> {

    public <R> FlatMap<R> flatten();

}
