package com.aol.cyclops2.control.transformers.seq;

import com.aol.cyclops2.types.AbstractTraversableTest;
import com.aol.cyclops2.types.Traversable;
import cyclops.monads.Witness;
import cyclops.stream.ReactiveSeq;


public class StreamTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        return ReactiveSeq.of(elements).liftM(Witness.reactiveSeq.CO_REACTIVE);
    }

    @Override
    public <T> Traversable<T> empty() {

        return ReactiveSeq.<T>empty().liftM(Witness.reactiveSeq.CO_REACTIVE);
    }

}
