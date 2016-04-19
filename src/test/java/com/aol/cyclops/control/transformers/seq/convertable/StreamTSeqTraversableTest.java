package com.aol.cyclops.control.transformers.seq.convertable;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.monads.transformers.StreamT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class StreamTSeqTraversableTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        return StreamT.fromIterable(ListX.of(ReactiveSeq.of(elements)));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return StreamT.emptyStream();
    }

}
