package com.aol.cyclops2.control.transformers.seq.convertable;

import com.aol.cyclops2.types.AbstractConvertableSequenceTest;
import com.aol.cyclops2.types.foldable.ConvertableSequence;
import cyclops.monads.Witness;
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
