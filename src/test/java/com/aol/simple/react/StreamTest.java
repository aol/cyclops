package com.aol.simple.react;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class StreamTest {

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
		
		
		List<String> strings = new SimpleReact()
								.<String>fromStream(new SimpleReact()
												.<Integer> react(() -> 1, () -> 2, () -> 3)
												.then(it -> "*" + it).stream())
								.then(it ->  it + "*")
								.block();

		assertThat(strings.size(), is(3));
		
		
		assertThat(strings,hasItem("*1*"));

	}
	
}
