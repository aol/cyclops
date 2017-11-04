package com.oath.cyclops.control.transformers.seq.convertable;

import com.oath.cyclops.types.AbstractConvertableSequenceTest;
import com.oath.cyclops.types.foldable.ConvertableSequence;
import cyclops.monads.DataWitness;
import cyclops.reactive.ReactiveSeq;


public class StreamTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        return ReactiveSeq.of(elements).liftM(Witness.list.INSTANCE).to();
    }

    @Override
    public <T> ConvertableSequence<T> empty() {

        return ReactiveSeq.<T>empty().liftM(Witness.list.INSTANCE).to();
    }

}
