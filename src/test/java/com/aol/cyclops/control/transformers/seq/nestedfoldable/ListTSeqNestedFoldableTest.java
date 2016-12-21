package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import java.util.Arrays;

import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;
import com.aol.cyclops.types.anyM.Witness;
import com.aol.cyclops.types.anyM.transformers.FoldableTransformerSeq;


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
