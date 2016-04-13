package com.aol.cyclops.control.transformers.seq.convertable;

import java.util.Optional;

import com.aol.cyclops.control.monads.transformers.OptionalT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class OptionalTSeqTraversableTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        
        return OptionalT.fromIterable(ListX.of(elements).map(Optional::of));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return OptionalT.emptyList();
    }

}
