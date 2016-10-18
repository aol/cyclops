package com.aol.cyclops.types;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.ConvertableSequence;

/**
 * An interface that represents a Functor that is not a single scalar value but a sequence,
 * and is convertable between sequential types.
 * 
 * @author johnmcclean
 *
 * @param <T> Data type of elements stored in this sequence
 */
public interface ConvertableSequentialFunctor<T> extends Functor<T>, ConvertableSequence<T>, ExtendedTraversable<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.types.stream.ConvertableSequence#stream()
     */
    @Override
    default ReactiveSeq<T> stream() {

        return ExtendedTraversable.super.stream();
    }

}
