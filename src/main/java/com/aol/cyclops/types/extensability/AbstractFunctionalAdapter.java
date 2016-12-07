package com.aol.cyclops.types.extensability;

import java.util.Iterator;
import java.util.function.Function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.types.anyM.WitnessType;

public abstract class  AbstractFunctionalAdapter<W extends WitnessType>  implements Comprehender<W>{

    @Override
    public abstract <T, R> AnyM<W, R> ap(AnyM<W, Function<T,R>> fn, AnyM<W, T> apply);

    @Override
    public <T, R> AnyM<W, R> map(AnyM<W, T> t, Function<? super T, ? extends R> fn) {
        return flatMap(t,fn.andThen(this::unit));
    }

    @Override
    public abstract <T, R> AnyM<W, R> flatMap(AnyM<W, T> t, Function<? super T, ? extends AnyM<W, ? extends R>> fn);

    @Override
    public abstract <T> AnyM<W, T> unitIterator(Iterator<T> it);

   
}
