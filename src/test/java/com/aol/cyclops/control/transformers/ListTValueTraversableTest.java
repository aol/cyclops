package com.aol.cyclops.control.transformers;

import java.util.Arrays;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.values.ListTValue;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class ListTValueTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return ListTValue.fromValue(Maybe.just(Arrays.asList(elements)));
    }

    @Override
    public <T> Traversable<T> empty() {
        return ListTValue.emptyOptional();
    }

}
