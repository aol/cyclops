package com.aol.cyclops.comprehensions;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

import lombok.val;

import org.junit.Test;

import com.aol.cyclops.comprehensions.LessTypingForComprehension3.Vars3;

public class CompletableFutureTest {

	@Test
	public void cf(){
		
		
		val f = CompletableFuture.completedFuture("hello world");
		val f2 = CompletableFuture.completedFuture("2");
		val f3 = CompletableFuture.completedFuture("3");
		CompletableFuture<String> result = ForComprehensions.foreach3(c -> c.flatMapAs$1(f)
										.flatMapAs$2((Vars3<String,String,String> v)->f2)
										.mapAs$3(v->f3)
										.yield(v-> v.$1()+v.$2()+v.$3())
									);
		
		assertThat(result.join(),equalTo("hello world23"));
	}

	@Test
	public void cfFromCallable(){
		
		
		val f = CompletableFuture.completedFuture("hello world");
		val f2 = CompletableFuture.completedFuture("2");
		val f3 = CompletableFuture.completedFuture("3");
		CompletableFuture<String> result = 
				ForComprehensions.foreach3(c -> c.flatMapAs$1((Callable)()->"hello world")
										.flatMapAs$2((Vars3<String,String,String> v)->f2)
										.mapAs$3(v->f3)
										.yield(v-> v.$1()+v.$2()+v.$3())
									);
		
		assertThat(result.join(),equalTo("hello world23"));
	}
	@Test
	public void cfFromSupplier(){
		
		
		val f = CompletableFuture.completedFuture("hello world");
		val f2 = CompletableFuture.completedFuture("2");
		val f3 = CompletableFuture.completedFuture("3");
		CompletableFuture<String> result = 
				ForComprehensions.foreach3(c -> c.flatMapAs$1((Supplier)()->(Supplier)()->"hello world")
										.flatMapAs$2((Vars3<String,String,String> v)->f2)
										.mapAs$3(v->f3)
										.yield(v-> v.$1()+v.$2()+v.$3())
									);
		
		assertThat(result.join(),equalTo("hello world23"));
	}
}
