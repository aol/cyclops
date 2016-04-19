package com.aol.cyclops.control.transformers.seq.convertable;

import com.aol.cyclops.control.Try;
import com.aol.cyclops.control.monads.transformers.TryT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class TryTSeqTraversableTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        
        return TryT.fromIterable(ListX.of(elements).map(Try::success));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return TryT.emptyList();
    }

}
