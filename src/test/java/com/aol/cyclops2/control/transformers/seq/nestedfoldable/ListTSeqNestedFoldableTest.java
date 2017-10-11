package com.aol.cyclops2.control.transformers.seq.nestedfoldable;

import cyclops.collectionx.mutable.ListX;
import com.aol.cyclops2.types.AbstractNestedFoldableTest;
import cyclops.monads.Witness;
import com.aol.cyclops2.types.anyM.transformers.FoldableTransformerSeq;


public class ListTSeqNestedFoldableTest extends AbstractNestedFoldableTest<Witness.list> {

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> of(T... elements) {
        return  ListX.of(elements).liftM(Witness.list.INSTANCE);
    }

    @Override
    public <T> FoldableTransformerSeq<Witness.list,T> empty() {
        return  ListX.<T>empty().liftM(Witness.list.INSTANCE);
    }

}
