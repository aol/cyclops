package com.aol.cyclops.types;

import com.aol.cyclops.types.sequence.ConvertableSequence;

public interface ConvertableSequentialFunctor<T> extends Functor<T>, ConvertableSequence<T>,
															ExtendedTraversable<T> {

}
