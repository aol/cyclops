package com.aol.cyclops.control.transformers.seq.convertable;

import java.util.Arrays;

import com.aol.cyclops.control.monads.transformers.ListT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class ListTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        return ListT.fromIterable(ListX.of(Arrays.asList(elements)));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return ListT.emptyList();
    }

}
