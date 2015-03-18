package com.aol.simple.react.simple;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.simple.SimpleReact;

public class StreamTest {

	
	@Test
	public void stackOverflow(){
		 Set<Long> set =  LazyFutureStream.parallelBuilder(10)
          .of("1.txt")
          .flatMap(x -> stage1(x)) 
          .map(x -> stage2(x))
          .map(x -> stage3(x))
          .collect(Collectors.toSet());
		 assertThat(set.size(),greaterThan(1));
	}
	private Object stage2(Object x) {
	
		return null;
	}
	private Long  stage3(Object x) {
	
		return Thread.currentThread().getId();
	}
	private Stream stage1(String x) {
		return Stream.of("hello","hello","world","test","world","test","hello","world","test","hello","world","test");
	}
	@Test
	public void testStreamFrom() throws InterruptedException,
			ExecutionException {
		
		
		List<String> strings = new SimpleReact()
								.<String>fromStream(new SimpleReact()
												.<Integer> react(() -> 1, () -> 2, () -> 3)
												.with(it -> "*" + it).stream())
								.then(it ->  it + "*")
								.block();

		assertThat(strings.size(), is(3));
		
		
		assertThat(strings,hasItem("*1*"));

	}
	@Test
	public void testStreamOf() throws InterruptedException,
			ExecutionException {
		
		Stream<CompletableFuture<String>> stream = new SimpleReact()
													.<Integer> react(() -> 1, () -> 2, () -> 3)
													.then(it -> "*" + it).streamCompletableFutures();

		List<String> strings = new SimpleReact()
								.<String>fromStream(stream)
								.then(it ->  it + "*")
								.block();

		assertThat(strings.size(), is(3));
		
		
		assertThat(strings,hasItem("*1*"));

	}
	
}
