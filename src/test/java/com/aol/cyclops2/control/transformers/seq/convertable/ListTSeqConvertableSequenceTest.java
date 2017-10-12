package com.aol.cyclops2.control.transformers.seq.convertable;

import cyclops.collectionx.mutable.ListX;
import com.aol.cyclops2.types.AbstractConvertableSequenceTest;
import cyclops.control.anym.Witness;
import com.aol.cyclops2.types.foldable.ConvertableSequence;


public class ListTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        return ListX.of(elements).liftM(Witness.list.INSTANCE).to();
    }

    @Override
    public <T> ConvertableSequence<T> empty() {

        return ListX.<T>empty().liftM(Witness.list.INSTANCE).to();
    }

}
