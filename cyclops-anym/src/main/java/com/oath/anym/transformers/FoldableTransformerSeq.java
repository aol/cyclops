package com.oath.anym.transformers;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.oath.cyclops.types.functor.FilterableTransformable;
import com.oath.anym.NestedCollectable;
import cyclops.monads.AnyM;
import cyclops.reactive.ReactiveSeq;
import com.oath.anym.NestedFoldable;
import cyclops.monads.WitnessType;
import com.oath.cyclops.types.stream.HeadAndTail;

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
    /**
     * extract head and tail together, where head is expected to be present
     *
     *
     * @return
     */
    default AnyM<W,HeadAndTail<T>> headAndTail() {
        return transformerStream().map(s -> s.headAndTail());
    }
    /**
     * Destructures this Traversable into it's head and tail. If the traversable instance is not a SequenceM or Stream type,
     * whenStream may be more efficient (as it is guaranteed to be lazy).
     *
     * <pre>
     * {@code
     * ListX.of(1,2,3,4,5,6,7,8,9)
    .dropRight(5)
    .plus(10)
    .visit((x,xs) ->
    xs.join(x.>2?"hello":"world")),()->"NIL"
    );
     *
     * }
     * //2world3world4
     *
     * </pre>
     *
     *
     * @param match
     * @return
     */
    default <R> AnyM<W,R> visit(final BiFunction<? super T, ? super ReactiveSeq<T>, ? extends R> match, final Supplier<? extends R> ifEmpty) {
        return transformerStream().map(s -> s.visit(match, ifEmpty));
    }

}
