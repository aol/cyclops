package com.aol.simple.react;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Test;

import com.aol.simple.react.stream.simple.SimpleReact;

public class FilterTest {

	@Test
	public void testFilterBehavesAsStreamFilter() throws InterruptedException,
			ExecutionException {
		 int expected = Arrays.asList("*1","*2","*3").stream().filter(it -> it.startsWith("*"))
		 .collect(Collectors.toList()).size();
		List<String> result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> "*" + it)
				.filter(it -> it.startsWith("*"))
				.block();

		assertThat(result.size(), is(expected));

	}
	@Test
	public void testNegativeFilterBehavesAsStreamFilter() throws InterruptedException,
			ExecutionException {
		 int expected = Arrays.asList("*1","*2","*3").stream().filter(it -> !it.startsWith("*"))
		 .collect(Collectors.toList()).size();
		List<String> result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> "*" + it)
				.filter(it -> !it.startsWith("*"))
				.block();

		assertThat(result.size(), is(expected));

	}
	@Test
	public void testFilter() throws InterruptedException,
			ExecutionException {
		 
		List<String> result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> "*" + it)
				.filter(it -> it.startsWith("*"))
				.block();

		assertThat(result.size(), is(3));

	}
	@Test
	public void testNegativeFilter() throws InterruptedException,
			ExecutionException {
		List<String> result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.then(it -> "*" + it)
				.filter(it -> !it.startsWith("*"))
				.block();

		assertThat(result.size(), is(0));

	}
	
	@Test
	public void testFilterFirst() throws InterruptedException,
			ExecutionException {
		 
		List<String> result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.filter(it -> 1!=it)
				.peek(it -> System.out.println(it))
				.<String>then(it -> "*" + it)
				.capture( e -> e.printStackTrace())
				.block();

		assertThat(result.size(), is(2));
		assertThat(result, not(hasItem("*1")));

	}
	@Test
	public void testFilterExceptions() throws InterruptedException,
			ExecutionException {
		 
		List<String> result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3)
				.filter(it -> 1!=it)
				.<String>then(it -> "*" + it)
				.capture( e -> fail("No exception should be captured"))
				.block();

		assertThat(result.size(), is(2));
		assertThat(result, not(hasItem("*1")));

	}
}
