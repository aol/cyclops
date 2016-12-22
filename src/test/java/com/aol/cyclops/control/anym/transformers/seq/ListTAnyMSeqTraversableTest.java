package com.aol.cyclops.control.anym.transformers.seq;

import cyclops.collections.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;
import cyclops.monads.Witness;


public class ListTAnyMSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return ListX.of(elements).liftM(Witness.list.INSTANCE);
    }

    @Override
    public <T> Traversable<T> empty() {
        return ListX.<T>empty().liftM(Witness.list.INSTANCE);
    }

}
