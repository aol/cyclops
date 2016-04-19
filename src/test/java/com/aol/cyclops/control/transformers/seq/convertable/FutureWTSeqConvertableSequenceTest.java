package com.aol.cyclops.control.transformers.seq.convertable;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.monads.transformers.FutureWT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class FutureWTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        
        return FutureWT.fromIterable(ListX.of(elements).map(FutureW::ofResult));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return FutureWT.emptyList();
    }

}
