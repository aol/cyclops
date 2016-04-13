package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import java.util.Arrays;
import java.util.Optional;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;



public class ListTValueNestedFoldableTest extends AbstractNestedFoldableTest {

    @Override
    public <T> NestedFoldable<T> of(T... elements) {
        return ListTValue.fromValue(Maybe.just(Arrays.asList(elements)));
    }

    @Override
    public <T> NestedFoldable<T> empty() {
        return ListT.fromOptional(Optional.of(ListX.empty()));
    }

}
