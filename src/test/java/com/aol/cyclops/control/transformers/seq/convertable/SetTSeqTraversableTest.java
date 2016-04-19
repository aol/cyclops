package com.aol.cyclops.control.transformers.seq.convertable;

import com.aol.cyclops.control.monads.transformers.SetT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.SetX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class SetTSeqTraversableTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        return SetT.fromIterable(ListX.of(SetX.of(elements)));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return SetT.emptySet();
    }

}
