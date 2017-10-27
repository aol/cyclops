package com.oath.cyclops.control.anym.transformers.seq;

import com.oath.cyclops.types.AbstractTraversableTest;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.collections.mutable.ListX;
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
