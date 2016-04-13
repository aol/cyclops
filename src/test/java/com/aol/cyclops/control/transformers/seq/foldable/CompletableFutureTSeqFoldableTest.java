package com.aol.cyclops.control.transformers.seq.foldable;

import java.util.concurrent.CompletableFuture;

import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.Foldable;


public class CompletableFutureTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> Foldable<T> of(T... elements) {
        
        return CompletableFutureT.fromIterable(ListX.of(elements).map(CompletableFuture::completedFuture));
    }


}
