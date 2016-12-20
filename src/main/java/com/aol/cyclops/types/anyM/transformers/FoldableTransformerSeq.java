package com.aol.cyclops.types.anyM.transformers;

import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.FilterableFunctor;
import com.aol.cyclops.types.anyM.NestedCollectable;
import com.aol.cyclops.types.anyM.NestedFoldable;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.stream.CyclopsCollectable;
import com.aol.cyclops.types.stream.HeadAndTail;

/**
 * A type that represents a non-scalar Monad Transformer
 * @author johnmcclean
 *
 * @param <T> Data type of elements in nested Monad
 */
public interface FoldableTransformerSeq<W extends WitnessType<W>,T> extends TransformerSeq<W,T>,  
                                                                            NestedFoldable<W,T>, 
                                                                            NestedCollectable<W,T>, 
                                                                            FilterableFunctor<T> {

    /* (non-Javadoc)
     * @see com.aol.cyclops.control.monads.transformers.values.TransformerSeq#stream()
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