package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.values.StreamableTValue;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;
import com.aol.cyclops.util.stream.Streamable;


public class StreamableTValueNestedFoldableTest extends AbstractNestedFoldableTest {

    
    @Override
    public <T> NestedFoldable<T> of(T... elements) {
        return StreamableTValue.fromValue(Maybe.just(Streamable.of(elements)));
    }

    @Override
    public <T> NestedFoldable<T> empty() {
        return StreamableTValue.fromValue(Maybe.just(Streamable.empty()));
    }

}
