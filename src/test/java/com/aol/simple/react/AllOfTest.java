package com.aol.simple.react;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import org.junit.Test;

public class AllOfTest {

	@Test
	public void testAllOfToSet() throws InterruptedException, ExecutionException {

		Set<Integer> result = new SimpleReact()
		.<Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.allOf(Collectors.toSet(), it -> {
			assertThat (it,is( Set.class));
			return it;
		}).blockAndExtract(Extractors.first());

		assertThat(result.size(),is(4));
	}
	
	
	@Test
	public void testAllOfParallelStreams() throws InterruptedException,
			ExecutionException {

		Integer result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
				.<Integer> then(it -> {
					return it * 200;
				})
				.then((Integer it) -> {
					if (it == 1000)
						throw new RuntimeException("boo!");

					return it;
				})
				.onFail(e -> 100)
				.<Integer,Integer>allOf(it -> {
					
					return it.parallelStream().filter(f -> f > 300)
							.map(m -> m - 5)
							.reduce(0, (acc, next) -> acc + next);
				}).block(Collectors.reducing(0, (acc,next)-> next));

	
		assertThat(result, is(990));
	}
	
	@Test
	public void testAllOfParallelStreamsSkip() throws InterruptedException,
			ExecutionException {

		List<Integer> result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
				.<Integer> then(it -> {
					return it * 200;
				})
				.then((Integer it) -> {
					if (it == 1000)
						throw new RuntimeException("boo!");

					return it;
				})
				.onFail(e -> 100)
				.<Integer,List<Integer>>allOf(it -> {
					
					return it.parallelStream().skip(1).limit(3).collect(Collectors.toList());
				}).first();

	
		assertThat(result.size(), is(3));
	}
	
	@Test
	public void testAllOfParallelStreamsSameForkJoinPool() throws InterruptedException,
			ExecutionException {
		Set<String> threadGroup = Collections.synchronizedSet(new TreeSet());
		Integer result = new SimpleReact()
				.<Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
				.<Integer> then(it -> {
					threadGroup.add(Thread.currentThread().getThreadGroup().getName());
					return it * 200;
				})
				.then((Integer it) -> {
					if (it == 1000)
						throw new RuntimeException("boo!");

					return it;
				})
				.onFail(e -> 100)
				.<Integer,Integer>allOf(it -> {
					
					return it.parallelStream().filter(f -> f > 300)
							.map(m ->{ threadGroup.add(Thread.currentThread().getThreadGroup().getName());return m - 5; })
							.reduce(0, (acc, next) -> acc + next);
				}).block(Collectors.reducing(0, (acc,next)-> next));

	
		assertThat(threadGroup.size(), is(1));
	}

	@Test
	public void testAllOf() throws InterruptedException, ExecutionException {

		boolean blocked[] = { false };

		new SimpleReact().<Integer> react(() -> 1, () -> 2, () -> 3)

		.then(it -> {
			try {
				Thread.sleep(50000);
			} catch (Exception e) {

			}
			blocked[0] = true;
			return 10;
		}).allOf(it -> it.size());

		assertThat(blocked[0], is(false));
	}
	
}
