package com.oath.cyclops.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.CompletableFuture;

import cyclops.companion.CompletableFutures;
import cyclops.data.Seq;
import cyclops.data.Vector;
import cyclops.reactive.ReactiveSeq;
import org.junit.Before;
import org.junit.Test;


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
        CompletableFuture<ReactiveSeq<Integer>> maybes = CompletableFutures.sequence(ReactiveSeq.of(just,none));
        assertThat(maybes.isCompletedExceptionally(),equalTo(true));
    }
    @Test
    public void testSequenceErrorAsync() {
        CompletableFuture<ReactiveSeq<Integer>> maybes =CompletableFutures.sequence(Seq.of(just,active));
        assertThat(maybes.isDone(),equalTo(false));
    }
    @Test
    public void testSequenceTwo() {
        CompletableFuture<ReactiveSeq<Integer>> maybes =CompletableFutures.sequence(Vector.of(just,just2));
        assertThat(maybes.join().toList(),equalTo(Arrays.asList(10,20)));
    }

}
