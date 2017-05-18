package com.aol.cyclops2.control.transformers.seq.convertable;

import cyclops.collections.mutable.ListX;
import com.aol.cyclops2.types.AbstractConvertableSequenceTest;
import cyclops.monads.Witness;
import com.aol.cyclops2.types.stream.ConvertableSequence;


public class ListTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        return ListX.of(elements).liftM(Witness.list.INSTANCE);
    }

    @Override
    public <T> ConvertableSequence<T> empty() {

        return ListX.<T>empty().liftM(Witness.list.INSTANCE);
    }

}
