package com.oath.cyclops.anym.extensability;


import com.oath.cyclops.anym.AnyMValue;
import cyclops.control.Option;

import cyclops.monads.WitnessType;

import java.util.function.Function;

public interface ValueAdapter<W extends WitnessType<W>> extends MonadAdapter<W> {

    default <R> R fold(Function<? super MonadAdapter<W>,? extends R> fn1, Function<? super  ValueAdapter<W>, ? extends R> fn2){
        return fn2.apply(this);
    }
    <T> Option<T> get(AnyMValue<W,T> t);
}
