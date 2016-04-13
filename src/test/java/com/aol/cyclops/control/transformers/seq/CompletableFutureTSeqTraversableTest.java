package com.aol.cyclops.control.transformers.seq;

import java.util.concurrent.CompletableFuture;

import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractTraversableTest;
import com.aol.cyclops.types.Traversable;


public class CompletableFutureTSeqTraversableTest extends AbstractTraversableTest {

    @Override
    public <T> Traversable<T> of(T... elements) {
        
        return CompletableFutureT.fromIterable(ListX.of(elements).map(CompletableFuture::completedFuture));
    }

    @Override
    public <T> Traversable<T> empty() {
        return CompletableFutureT.emptyList();
    }

}
