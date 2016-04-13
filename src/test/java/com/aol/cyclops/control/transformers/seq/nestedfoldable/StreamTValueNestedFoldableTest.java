package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import java.util.Optional;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.control.monads.transformers.values.StreamTValue;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;


public class StreamTValueNestedFoldableTest extends AbstractNestedFoldableTest {

    @Override
    public <T> NestedFoldable<T> of(T... elements) {
        return StreamT.fromOptional(Optional.of(ReactiveSeq.of(elements)));
    }

    @Override
    public <T> NestedFoldable<T> empty() {
        return StreamTValue.fromValue(Maybe.just(ReactiveSeq.empty()));
    }

}
