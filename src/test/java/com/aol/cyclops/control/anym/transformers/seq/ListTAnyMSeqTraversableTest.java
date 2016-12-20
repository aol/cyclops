package com.aol.cyclops.control.anym.transformers.seq;

import java.util.Arrays;

import com.aol.cyclops.control.AnyM;
import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.anyM.Witness;


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
