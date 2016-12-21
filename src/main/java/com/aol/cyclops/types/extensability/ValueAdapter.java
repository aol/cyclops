package com.aol.cyclops.types.extensability;

import com.aol.cyclops.types.anyM.AnyMValue;
import com.aol.cyclops.types.anyM.WitnessType;

import java.util.function.Function;

public interface ValueAdapter<W extends WitnessType<W>> extends FunctionalAdapter<W> {

    default <R> R visit(Function<? super FunctionalAdapter<W>,? extends R> fn1, Function<? super  ValueAdapter<W>, ? extends R> fn2){
        return fn2.apply(this);
    }
    <T> T get(AnyMValue<W,T> t);
}
