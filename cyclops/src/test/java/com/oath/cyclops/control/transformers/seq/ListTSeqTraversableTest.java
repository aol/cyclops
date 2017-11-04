package com.oath.cyclops.control.transformers.seq;

import com.oath.cyclops.types.AbstractTraversableTest;
import cyclops.collections.mutable.ListX;
import com.oath.cyclops.types.traversable.Traversable;
import cyclops.monads.DataWitness;


public class ListTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return ListX.of(elements).liftM(Witness.list.INSTANCE);
    }

    @Override
    public <T> Traversable<T> empty() {

        return ListX.<T>empty().liftM(Witness.list.INSTANCE);
    }

}
