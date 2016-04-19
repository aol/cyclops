package com.aol.cyclops.control.transformers.seq.convertable;

import com.aol.cyclops.control.Xor;
import com.aol.cyclops.control.monads.transformers.XorT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class XorTSeqTraversableTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        
        return XorT.fromIterable(ListX.of(elements).map(Xor::primary));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return XorT.emptyList();
    }

}
