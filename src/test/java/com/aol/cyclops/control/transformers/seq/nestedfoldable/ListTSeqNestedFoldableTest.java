package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import java.util.Arrays;

import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;


public class ListTSeqNestedFoldableTest extends AbstractNestedFoldableTest {

    @Override
    public <T> NestedFoldable<T> of(T... elements) {
        return ListT.fromIterable(ListX.of(Arrays.asList(elements)));
    }

    @Override
    public <T> NestedFoldable<T> empty() {
        return ListT.fromIterable(ListX.of(ListX.<T>empty()));
    }

}
