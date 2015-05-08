package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.CompletableFuture;

import lombok.val;

import org.junit.Test;

public class CompletableFutureTest {

	@Test
	public void cf(){
		val comp = new ForComprehension3<CompletableFuture,CompletableFuture<String>,String>();
		
		val f = CompletableFuture.completedFuture("hello world");
		val f2 = CompletableFuture.completedFuture("2");
		val f3 = CompletableFuture.completedFuture("3");
		val result = comp.<String,String,String>foreach(c -> c.flatMapAs$1(f)
										.flatMapAs$2(f2)
										.mapAs$3(f3)
										.yield(()-> c.$1()+c.$2()+c.$3())
									);
		
		assertThat(result.join(),equalTo("hello world23"));
	}
}
