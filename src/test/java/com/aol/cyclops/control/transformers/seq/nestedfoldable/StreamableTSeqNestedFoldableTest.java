package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import java.util.Arrays;

import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;


public class StreamableTSeqNestedFoldableTest extends AbstractNestedFoldableTest {

    
    @Override
    public <T> NestedFoldable<T> of(T... elements) {
        return StreamableT.fromIterable(Arrays.asList(Streamable.of(elements)));
    }

    @Override
    public <T> NestedFoldable<T> empty() {
        return StreamableT.fromIterable(Streamable.of(Streamable.empty()));
    }

}
