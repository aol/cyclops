package com.oath.cyclops.anym.transformers;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oath.cyclops.types.functor.FilterableTransformable;
import com.oath.cyclops.anym.NestedCollectable;
import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import com.oath.cyclops.anym.NestedFoldable;
import cyclops.monads.WitnessType;

/**
 * A type that represents a non-scalar Monad Transformer
 * @author johnmcclean
 *
 * @param <T> Data type of elements in nest Monad
 */
public interface FoldableTransformerSeq<W extends WitnessType<W>,T> extends TransformerSeq<W,T>,
                                                                            NestedFoldable<W,T>,
  NestedCollectable<W,T>,
        FilterableTransformable<T> {

    /* (non-Javadoc)
     * @see cyclops2.monads.transformers.values.TransformerSeq#stream()
     */
    @Override
    default ReactiveSeq<T> stream() {
        return TransformerSeq.super.stream();
    }
    public <R> FoldableTransformerSeq<W,R> empty();
    public <R> FoldableTransformerSeq<W,R> unitIterator(final Iterator<R> it);
    public <B> FoldableTransformerSeq<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f);


}
