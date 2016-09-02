package com.aol.cyclops.control.transformers.seq.convertable;

import java.util.Arrays;

import com.aol.cyclops.control.Streamable;
import com.aol.cyclops.control.monads.transformers.StreamableT;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class StreamableTSeqTraversableTest extends AbstractConvertableSequenceTest {

    
    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        return StreamableT.fromIterable(Arrays.asList(Streamable.of(elements)));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return StreamableT.emptyStreamable();
    }

}
