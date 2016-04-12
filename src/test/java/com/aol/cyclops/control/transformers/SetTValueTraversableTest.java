package com.aol.cyclops.control.transformers;

import java.util.Optional;

import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.control.monads.transformers.values.SetTValue;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class SetTValueTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return SetT.fromOptional(Optional.of(SetX.of(elements)));
    }

    @Override
    public <T> Traversable<T> empty() {
        return SetTValue.emptyOptional();
    }

}
