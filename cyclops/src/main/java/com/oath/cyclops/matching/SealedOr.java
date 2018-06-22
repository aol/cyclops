package com.oath.cyclops.matching;


import java.util.function.Function;
import java.util.function.Supplier;

public interface SealedOr<T1> {

    public <R> R fold(Function<? super T1, ? extends R> fn1, Supplier<? extends R> s);

}
