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

public class BlockingTest {

	
	@Test
	public void testBlockStreamsSeparateExecutors() throws InterruptedException,
			ExecutionException {

		Integer result = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 200).<Integer> block().parallelStream()
				.filter(f -> f > 300).map(m -> m - 5)
				.reduce(0, (acc, next) -> acc + next);

		assertThat(result, is(990));
	}

	
	
	@Test
	public void testBlockStreamsSameForkJoinPool() throws InterruptedException,
			ExecutionException {
		
		//See same test in ResultCollectionTest for a better way to do this
		Set<String> threadGroup = Collections.synchronizedSet(new TreeSet());
		Stage<Integer,Integer> builder = new SimpleReact()
		.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
		.then((it) -> { threadGroup.add(Thread.currentThread().getThreadGroup().getName()); return it * 200;});
		int result = builder.submit (  () -> 
				 builder.<Integer>block().parallelStream()
				.filter(f -> f > 300).map(m ->{ threadGroup.add(Thread.currentThread().getThreadGroup().getName());return m - 5; })
				.reduce(0, (acc, next) -> acc + next));

		assertThat(result, is(990));
		assertThat(threadGroup.size(), is(1));
	}
	
	@Test
	public void testBlock() throws InterruptedException, ExecutionException {

		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> "*" + it).block();

		assertThat(strings.size(), is(3));

	}
	@Test
	public void testBlockToSet() throws InterruptedException, ExecutionException {

		Set<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 1, () -> 3)
				.then((it) -> it * 100).then((it) -> "*" + it).block(Collectors.toSet());

		assertThat(strings.size(), is(2));

	}
	
	@Test
	public void testBreakout() throws InterruptedException, ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it)
				.block(status -> status.getCompleted() > 1);

		assertThat(strings.size(), is(2));

	}
	@Test
	public void testBreakoutToSet() throws InterruptedException, ExecutionException {
		Throwable[] error = { null };
		Set<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it)
				.block(Collectors.toSet(),status -> status.getCompleted() > 1);

		assertThat(strings.size(), is(2));

	}

	@Test
	public void testBreakoutException() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {

					throw new RuntimeException("boo!");

				}).capture(e -> error[0] = e)
				.block(status -> status.getCompleted() >= 1);

		assertThat(strings.size(), is(0));
		assertThat(error[0], is(RuntimeException.class));
	}
	volatile int count =0;
	@Test
	public void testBreakoutExceptionTimes() throws InterruptedException,
			ExecutionException {
		count =0;
		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {

					throw new RuntimeException("boo!");

				}).capture(e -> count++)
				.block(status -> status.getCompleted() >= 1);

		assertThat(strings.size(), is(0));
		assertThat(count, is(3));
	}
	@Test
	public void testBreakoutAllCompleted() throws InterruptedException,
			ExecutionException {
		count =0;
		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {
					if(it==100)
						throw new RuntimeException("boo!");
					else
						sleep(it);
					return it;

				}).capture(e -> count++)
				.block(status -> status.getAllCompleted() >0);

		assertThat(strings.size(), is(0));
		assertThat(count, is(1));
	}
	@Test
	public void testBreakoutAllCompletedAndTime() throws InterruptedException,
			ExecutionException {
		count =0;
		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {
					sleep(it);
					return it;

				}).capture(e -> count++)
				.block(status -> status.getAllCompleted() >1 && status.getElapsedMillis()>200);

		assertThat(strings.size(), is(2));
		assertThat(count, is(0));
	}
	

	@Test
	public void testBreakoutInEffective() throws InterruptedException,
			ExecutionException {
		Throwable[] error = { null };
		List<String> strings = new SimpleReact()
				.<Integer, Integer> react(() -> 1, () -> 2, () -> 3)
				.then((it) -> it * 100).then((it) -> {
					if (it == 100)
						throw new RuntimeException("boo!");

					return it;
				}).onFail(e -> 1).then((it) -> "*" + it)
				.block(status -> status.getCompleted() > 5);

		assertThat(strings.size(), is(3));

	}
	@Test
	public void testLast() throws InterruptedException, ExecutionException {

		Integer result = new SimpleReact()
		.<Integer, Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.then( it -> sleep(it))
		.last();

		assertThat(result,is(500));
	}
	 
	

	@Test
	public void testFirst() throws InterruptedException, ExecutionException {

		Set<Integer> result = new SimpleReact()
		.<Integer, Integer> react(() -> 1, () -> 2, () -> 3, () -> 5)
		.then( it -> it*100)
		.allOf(Collectors.toSet(), it -> {
			assertThat (it,is( Set.class));
			return it;
		}).first();

		assertThat(result.size(),is(4));
	}
	
	private Object sleep(Integer it) {
		try {
			Thread.sleep(it);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return it;
	}
}
