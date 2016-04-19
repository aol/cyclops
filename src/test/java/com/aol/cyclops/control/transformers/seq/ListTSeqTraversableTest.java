package com.aol.cyclops.control.transformers.seq;

import java.util.Arrays;

import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class ListTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return ListT.fromIterable(ListX.of(Arrays.asList(elements)));
    }

    @Override
    public <T> Traversable<T> empty() {
        return ListT.emptyList();
    }

}
