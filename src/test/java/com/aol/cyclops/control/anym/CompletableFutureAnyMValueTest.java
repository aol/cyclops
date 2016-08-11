package com.aol.cyclops.control.anym;

import java.util.concurrent.CompletableFuture;

import org.junit.Before;

import com.aol.cyclops.control.AnyM;

public class CompletableFutureAnyMValueTest extends BaseAnyMValueTest {
    @Before
    public void setUp() throws Exception {
        just = AnyM.fromCompletableFuture(CompletableFuture.completedFuture(10));
        CompletableFuture error = new CompletableFuture();
        error.completeExceptionally(new RuntimeException());
        none = AnyM.fromCompletableFuture(error);
    }
    
}
