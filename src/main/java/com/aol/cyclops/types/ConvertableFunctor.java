package com.aol.cyclops.types;

import java.util.function.Function;

public interface ConvertableFunctor<T> extends Value<T>, Functor<T> {

    @Override
    <R> ConvertableFunctor<R> map(Function<? super T, ? extends R> fn);
}
