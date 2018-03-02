package com.oath.anym.transformers;

import java.util.function.BiPredicate;
import java.util.function.Predicate;

import cyclops.data.Vector;
import cyclops.monads.Witness.reactiveSeq;
import cyclops.monads.Witness;
import cyclops.monads.transformers.SeqT;
import cyclops.monads.transformers.VectorT;
import cyclops.reactive.ReactiveSeq;
import cyclops.monads.transformers.ListT;
import cyclops.reactive.collections.mutable.ListX;

/**
 * Represents a Traversable Monad Transformer, the monad transformer instance manipulates a nested non-scalar data type
 *
 * @author johnmcclean
 *
 * @param <T> Data type of the elements stored inside the traversable manipulated by this monad transformer
 */
public interface TransformerTraversable<T>{


    default VectorT<reactiveSeq,T> groupedT(final int groupSize) {
        return VectorT.fromStream(stream().grouped(groupSize));
    }


    default SeqT<reactiveSeq,T> slidingT(final int windowSize, final int increment) {
        return SeqT.fromStream(stream().sliding(windowSize, increment));
    }


    default SeqT<reactiveSeq,T> slidingT(final int windowSize) {
        return SeqT.fromStream(stream().sliding(windowSize));
    }


    default VectorT<reactiveSeq,T> groupedUntilT(final Predicate<? super T> predicate) {
        return VectorT.fromStream(stream().groupedUntil(predicate));
    }


    default VectorT<reactiveSeq,T> groupedUntilT(final BiPredicate<Vector<? super T>, ? super T> predicate) {
        return VectorT.fromStream(stream().groupedUntil(predicate));
    }


    default VectorT<reactiveSeq,T> groupedWhileT(final Predicate<? super T> predicate) {
        return VectorT.fromStream(stream().groupedUntil(predicate));
    }

    public ReactiveSeq<T> stream();
}
