package com.aol.cyclops.types;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;

public interface ConvertableSequentialFunctor<T> extends Functor<T>, ConvertableSequence<T>, ExtendedTraversable<T> {

    @Override
    default ReactiveSeq<T> stream() {

        return ExtendedTraversable.super.stream();
    }

}
