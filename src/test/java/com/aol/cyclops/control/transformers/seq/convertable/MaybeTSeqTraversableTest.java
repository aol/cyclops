package com.aol.cyclops.control.transformers.seq.convertable;

import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.control.monads.transformers.MaybeT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class MaybeTSeqTraversableTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        
        return MaybeT.fromIterable(ListX.of(elements).map(Maybe::just));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return MaybeT.emptyList();
    }

}
