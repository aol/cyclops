package com.oath.anym.extensability;


import com.oath.anym.AnyMValue;
import cyclops.control.Option;

import cyclops.monads.WitnessType;

import java.util.function.Function;

public interface ValueAdapter<W extends WitnessType<W>> extends FunctionalAdapter<W> {

    default <R> R visit(Function<? super FunctionalAdapter<W>,? extends R> fn1, Function<? super  ValueAdapter<W>, ? extends R> fn2){
        return fn2.apply(this);
    }
    <T> Option<T> get(AnyMValue<W,T> t);
}
