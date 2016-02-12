package com.aol.cyclops.types;

import com.aol.cyclops.types.stream.ConvertableSequence;

public interface ConvertableSequentialFunctor<T> extends Functor<T>, ConvertableSequence<T>,
															ExtendedTraversable<T> {

}
