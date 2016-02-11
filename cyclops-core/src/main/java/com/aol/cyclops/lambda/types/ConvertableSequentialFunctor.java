package com.aol.cyclops.lambda.types;

import com.aol.cyclops.sequence.traits.ConvertableSequence;

public interface ConvertableSequentialFunctor<T> extends Functor<T>, ConvertableSequence<T>,
															ExtendedTraversable<T> {

}
