package com.oath.cyclops.control.transformers.seq.nestedfoldable;

import cyclops.collections.mutable.ListX;
import com.oath.cyclops.types.AbstractNestedFoldableTest;
import cyclops.monads.DataWitness;
import com.oath.anym.transformers.FoldableTransformerSeq;


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
