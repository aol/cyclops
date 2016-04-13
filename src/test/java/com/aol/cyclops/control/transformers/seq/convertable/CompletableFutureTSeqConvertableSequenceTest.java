package com.aol.cyclops.control.transformers.seq.convertable;

import java.util.concurrent.CompletableFuture;

import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractConvertableSequenceTest;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.types.stream.ConvertableSequence;


public class CompletableFutureTSeqConvertableSequenceTest extends AbstractConvertableSequenceTest {

    @Override
    public <T> ConvertableSequence<T> of(T... elements) {
        
        return CompletableFutureT.fromIterable(ListX.of(elements).map(CompletableFuture::completedFuture));
    }

    @Override
    public <T> ConvertableSequence<T> empty() {
        return CompletableFutureT.emptyList();
    }

}
