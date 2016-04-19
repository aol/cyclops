package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;


public class StreamTSeqNestedFoldableTest extends AbstractNestedFoldableTest {

    @Override
    public <T> NestedFoldable<T> of(T... elements) {
        return StreamT.fromIterable(ListX.of(ReactiveSeq.of(elements)));
    }

    @Override
    public <T> NestedFoldable<T> empty() {
        return StreamT.fromIterable(ListX.of(ReactiveSeq.empty()));
    }

}
