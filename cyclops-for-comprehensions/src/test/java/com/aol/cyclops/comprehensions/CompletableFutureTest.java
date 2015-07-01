package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.comprehensions.donotation.typed.Do;

public class CompletableFutureTest {

	@Test
	public void cf(){
		
		
		val f = CompletableFuture.completedFuture("hello world");
		val f2 = CompletableFuture.completedFuture("2");
		val f3 = CompletableFuture.completedFuture("3");
		CompletableFuture<String> result = Do.add(f)
											.add(f2)
											.add(f3) 
											.yield(v1->v2->v3 -> v1 +v2 +v3)
											.unwrap();
									
		
		assertThat(result.join(),equalTo("hello world23"));
	}

	@Test
	public void cfFromCallable(){
		
		
		val f = CompletableFuture.completedFuture("hello world");
		val f2 = CompletableFuture.completedFuture("2");
		val f3 = CompletableFuture.completedFuture("3");
		CompletableFuture<String> result =  Do.add((Callable<String>)()->"hello world")
												.add(f2)
												.add(f3)
												.yield(v1->v2->v3 -> v1 +v2 +v3).unwrap();
									
		
		assertThat(result.join(),equalTo("hello world23"));
	}
	@Test
	public void cfFromSupplier(){
		
		
		val f = CompletableFuture.completedFuture("hello world");
		val f2 = CompletableFuture.completedFuture("2");
		val f3 = CompletableFuture.completedFuture("3");
		CompletableFuture<String> result = Do.add((Supplier<Supplier<String>>)()->(Supplier<String>)()->"hello world")
											 .add(f2)
											 .add(f3)
											 .yield(v1->v2->v3 -> v1 +v2 +v3)
											 .unwrap();
		
		assertThat(result.join(),equalTo("hello world23"));
	}
}
