package com.aol.cyclops.control.transformers.seq.foldable;

import java.util.concurrent.CompletableFuture;

import com.aol.cyclops.control.monads.transformers.CompletableFutureT;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.types.AbstractFoldableTest;
import com.aol.cyclops.types.IterableFoldable;


public class CompletableFutureTSeqFoldableTest extends AbstractFoldableTest {

    @Override
    public <T> IterableFoldable<T> of(T... elements) {
        
        return CompletableFutureT.fromIterable(ListX.of(elements).map(CompletableFuture::completedFuture));
    }


}
