package com.aol.simple.react.simple;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class StreamTest {

	
	
	@Test
	public void testStreamFrom() throws InterruptedException,
			ExecutionException {
		
		
		List<String> strings = new SimpleReact()
								.<String>fromStream(new EagerReact()
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
