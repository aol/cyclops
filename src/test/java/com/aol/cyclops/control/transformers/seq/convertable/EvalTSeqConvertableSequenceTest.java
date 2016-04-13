package com.aol.cyclops.control.transformers.seq.convertable;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.monads.transformers.EvalT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class EvalTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        
        return EvalT.fromIterable(ListX.of(elements).map(Eval::now));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return EvalT.emptyList();
    }
   
}
