package com.aol.simple.react;

import static com.aol.simple.react.SimpleReact.iterate;
import static com.aol.simple.react.SimpleReact.times;
import static org.hamcrest.Matchers.*;
import static org.junit.Assert.assertThat;

import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class GeneratorTest {
	volatile int  count;
	@Test
	public void testGenerate() throws InterruptedException, ExecutionException {
		count =0;
		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> count++ ,SimpleReact.times(4))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(4));
		assertThat(count,is(4));
	}
	@Test
	public void testGenerateOffset() throws InterruptedException, ExecutionException {
		count =0;
		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> count++ ,times(1).offset(2))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(1));
		assertThat(count,greaterThan(1)); 
				//can't guarantee skip completablefutures will have completed
		
	}
	@Test
	public void testIterate() throws InterruptedException, ExecutionException {
		count =0;
		List<String> strings = new SimpleReact()
				.<String, String> react((input) -> input + count++,iterate("hello").times(10))
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(10));
		assertThat(count,is(9));

	}
	@Test
	public void testIterateWithOffset() throws InterruptedException, ExecutionException {
		
		List<Integer> results = new SimpleReact()
				.<Integer, Integer> react((input) -> input + 1,iterate(0).times(1).offset(10))
				.then(it -> it*100)
				.block();

		assertThat(results.size(), is(1));
		
		assertThat(results.get(0),is(1000));

	}
}
