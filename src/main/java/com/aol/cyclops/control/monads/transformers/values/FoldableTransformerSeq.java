package com.aol.cyclops.control.monads.transformers.values;

import java.util.Iterator;
import java.util.function.Function;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.FilterableFunctor;
import com.aol.cyclops.types.anyM.NestedCollectable;
import com.aol.cyclops.types.anyM.NestedFoldable;
import com.aol.cyclops.types.anyM.WitnessType;
import com.aol.cyclops.types.stream.CyclopsCollectable;

/**
 * A type that represents a non-scalar Monad Transformer
 * @author johnmcclean
 *
 * @param <T> Data type of elements in nested Monad
 */
public interface FoldableTransformerSeq<W extends WitnessType,T> extends TransformerSeq<W,T>,  
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
    AnyM<W,? extends CyclopsCollectable<T>> nestedCollectables();
    public <R> FoldableTransformerSeq<W,R> empty();
    public <R> FoldableTransformerSeq<W,R> unitIterator(final Iterator<R> it);
    public <B> FoldableTransformerSeq<W,B> flatMap(final Function<? super T, ? extends Iterable<? extends B>> f);
}
