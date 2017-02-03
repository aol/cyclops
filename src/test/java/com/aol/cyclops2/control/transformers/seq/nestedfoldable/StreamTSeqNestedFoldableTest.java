package com.aol.cyclops2.control.transformers.seq.nestedfoldable;

import com.aol.cyclops2.types.AbstractNestedFoldableTest;
import com.aol.cyclops2.types.anyM.transformers.FoldableTransformerSeq;
import cyclops.collections.ListX;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;


public class StreamTSeqNestedFoldableTest extends AbstractNestedFoldableTest<Witness.list> {

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> of(T... elements) {
        return  ReactiveSeq.of(elements).liftM(Witness.list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> empty() {
        return  ReactiveSeq.<T>empty().liftM(Witness.list.INSTANCE);
    }

}
