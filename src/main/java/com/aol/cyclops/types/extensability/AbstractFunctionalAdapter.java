package com.aol.cyclops.types.extensability;

import java.util.function.Function;

import cyclops.monads.AnyM;
import cyclops.monads.WitnessType;

public abstract class  AbstractFunctionalAdapter<W extends WitnessType<W>>  implements FunctionalAdapter<W>{

    @Override
    public abstract <T, R> AnyM<W, R> ap(AnyM<W,? extends Function<? super T,? extends R>> fn, AnyM<W, T> apply);

    @Override
    public <T, R> AnyM<W, R> map(AnyM<W, T> t, Function<? super T, ? extends R> fn) {
        return flatMap(t,fn.andThen(this::unit));
    }

    @Override
    public abstract <T, R> AnyM<W, R> flatMap(AnyM<W, T> t, Function<? super T, ? extends AnyM<W, ? extends R>> fn);

    @Override
    public abstract <T> AnyM<W, T> unitIterable(Iterable<T> it);

   
}
