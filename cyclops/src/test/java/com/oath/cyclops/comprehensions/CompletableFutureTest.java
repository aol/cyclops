package com.oath.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;

import cyclops.control.Future;
import cyclops.companion.CompletableFutures;
import org.junit.Test;

import lombok.val;

public class CompletableFutureTest {

	@Test
	public void cf(){


        CompletableFuture<String> f = CompletableFuture.completedFuture("hello world");
        CompletableFuture<String> f2 = CompletableFuture.completedFuture("2");
        CompletableFuture<String> f3 = CompletableFuture.completedFuture("3");
		CompletableFuture<String> result = CompletableFutures.forEach3(f,a->f2,(a,b)->f3,(v1,v2,v3) -> v1 +v2 +v3);


		assertThat(result.join(),equalTo("hello world23"));
	}

	@Test
	public void cfFromCallable(){


        Future<String> f = Future.of(CompletableFuture.completedFuture("hello world"));
        Future<String> f2 = Future.of(CompletableFuture.completedFuture("2"));
        Future<String> f3 = Future.of(CompletableFuture.completedFuture("3"));
		Future<String> result =  Future.of(()->"hello world").forEach3(a->f2,(a, b)->f3,(v1, v2, v3) -> v1 +v2 +v3);


		assertThat(result.getFuture().join(),equalTo("hello world23"));
	}
	@Test
	public void cfFromSupplier(){


        Future<String> f = Future.of(CompletableFuture.completedFuture("hello world"));
        Future<String> f2 = Future.of(CompletableFuture.completedFuture("2"));
        Future<String> f3 = Future.of(CompletableFuture.completedFuture("3"));
		Future<String> result = Future.of(()->"hello world").forEach3(a->f2,(a, b)->f3,(v1, v2, v3) -> v1 +v2 +v3);

		assertThat(result.getFuture().join(),equalTo("hello world23"));
	}
}
