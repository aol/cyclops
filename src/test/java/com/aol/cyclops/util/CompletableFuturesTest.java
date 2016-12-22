package com.aol.cyclops.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;

import cyclops.CompletableFutures;
import org.junit.Before;
import org.junit.Test;

import cyclops.collections.ListX;

public class CompletableFuturesTest {
    
    CompletableFuture<Integer> just;
    CompletableFuture<Integer> none;
    CompletableFuture<Integer> active;
    CompletableFuture<Integer> just2;
    
    @Before
    public void setup(){
        just = CompletableFuture.completedFuture(10);
        none = new CompletableFuture<>();
        none.completeExceptionally(new Exception("boo"));
        active = new CompletableFuture<>();
        just2 = CompletableFuture.completedFuture(20);
    }
    
    @Test
    public void testSequenceError() {
        CompletableFuture<ListX<Integer>> maybes = CompletableFutures.sequence(ListX.of(just,none));
        assertThat(maybes.isCompletedExceptionally(),equalTo(true));
    }
    @Test
    public void testSequenceErrorAsync() {
        CompletableFuture<ListX<Integer>> maybes =CompletableFutures.sequence(ListX.of(just,active));
        assertThat(maybes.isDone(),equalTo(false));
    }
    @Test
    public void testSequenceTwo() {
        CompletableFuture<ListX<Integer>> maybes =CompletableFutures.sequence(ListX.of(just,just2));
        assertThat(maybes.join(),equalTo(ListX.of(10,20)));
    }

}
