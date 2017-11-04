package com.oath.cyclops.control.transformers.seq.nestedfoldable;

import com.oath.cyclops.types.AbstractNestedFoldableTest;
import com.oath.anym.transformers.FoldableTransformerSeq;
import cyclops.monads.DataWitness;
import cyclops.reactive.ReactiveSeq;


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
