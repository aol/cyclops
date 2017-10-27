package com.oath.cyclops.matching;

import java.util.function.Function;

public interface Sealed2<T1,T2> {

    public <R> R fold(Function<? super T1, ? extends R> fn1, Function<? super T2, ? extends R> fn2);

}
