package com.aol.cyclops.control.transformers.seq.nestedfoldable;

import java.util.Optional;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.values.SetTValue;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.AbstractNestedFoldableTest;
import com.aol.cyclops.types.anyM.NestedFoldable;



public class SetTValueNestedFoldableTest extends AbstractNestedFoldableTest {

    @Override
    public <T> NestedFoldable<T> of(T... elements) {
        return SetT.fromOptional(Optional.of(SetX.of(elements)));
    }

    @Override
    public <T> NestedFoldable<T> empty() {
        return SetTValue.fromValue(Maybe.just(SetX.empty()));
    }

}
