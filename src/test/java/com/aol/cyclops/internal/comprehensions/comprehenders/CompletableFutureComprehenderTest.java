package com.aol.cyclops.internal.comprehensions.comprehenders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.junit.Test;
import org.testng.Assert;

/**
 * Created by weiliyang on 5/24/16.
 */
public class CompletableFutureComprehenderTest {

    @Test
    public void testFromIterator() throws ExecutionException, InterruptedException {
        CompletableFutureComprehender completableFutureComprehender = new CompletableFutureComprehender();
        List<String> strs = Arrays.asList("a", "b", "c");
        CompletableFuture completableFuture = completableFutureComprehender.fromIterator(strs.iterator());
        Object o = completableFuture.get();
        Assert.assertEquals(o, "a");
    }
    
    @Test
    public void testFromIteratorWithEmptyIterator() throws ExecutionException, InterruptedException {
        CompletableFutureComprehender completableFutureComprehender = new CompletableFutureComprehender();
        List<String> strs = new ArrayList();
        CompletableFuture completableFuture = completableFutureComprehender.fromIterator(strs.iterator());
        Object o = completableFuture.get();
        Assert.assertNull(o);
    }
}
