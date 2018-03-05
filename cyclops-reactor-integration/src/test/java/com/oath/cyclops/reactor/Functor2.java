package com.oath.cyclops.reactor;

import java.util.function.Function;

public interface Functor2<T> {

    <R> Functor2<R> map(Function<? super T, ? extends R> fn);
}
