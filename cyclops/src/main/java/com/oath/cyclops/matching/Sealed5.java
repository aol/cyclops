package com.oath.cyclops.matching;


import java.util.function.Function;

public interface Sealed5<T1,T2,T3,T4,T5> {

    public <R> R fold(Function<? super T1, ? extends R> fn1, Function<? super T2, ? extends R> fn2,
                      Function<? super T3, ? extends R> fn3, Function<? super T4, ? extends R> fn4,
                      Function<? super T5, ? extends R> fn5);




}
