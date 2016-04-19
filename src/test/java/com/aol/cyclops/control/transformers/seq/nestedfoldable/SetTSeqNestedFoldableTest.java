package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;


public class SetTSeqNestedFoldableTest extends AbstractNestedFoldableTest {

    @Override
    public <T> NestedFoldable<T> of(T... elements) {
        return SetT.fromIterable(ListX.of(SetX.of(elements)));
    }

    @Override
    public <T> NestedFoldable<T> empty() {
        return SetT.fromIterable(SetX.of(SetX.empty()));
    }

}
