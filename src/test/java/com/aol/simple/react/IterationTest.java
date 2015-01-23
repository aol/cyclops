package com.aol.simple.react;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Test;

public class IterationTest {

	@Test
	public void testIterate() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList(1,2,3,4);
		List<String> strings = new SimpleReact()
				.<Integer> react(list.iterator() ,list.size())
				.peek(it -> System.out.println(it))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(4));
		
	}
	@Test
	public void testReactWithCollection() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList(1,2,3,4);
		List<String> strings = new SimpleReact()
				.<Integer> reactToCollection(list)
				.peek(it -> System.out.println(it))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(4));
		
	}
	
	@Test
	public void testReactWithCollectionOfStrings() throws InterruptedException, ExecutionException {
		List<String> list = Arrays.asList("hello","world","$da^","along");
		int count  = new SimpleReact()
				.reactToCollection(list)
				.filter(it -> !it.startsWith("$"))
				.then(it -> it.length())
				.block().stream().reduce(0, (acc,next) -> acc+next );

		assertThat(count, is(15));
		
	}
	
	@Test
	public void testIterateLargeMaxSize() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList(1,2,3,4);
		List<String> strings = new SimpleReact()
				.<Integer> react(list.iterator() ,500)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(4));
		
	}
	@Test
	public void testIterateEmptyIterator() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList();
		List<String> strings = new SimpleReact()
				.<Integer> react(list.iterator() ,1)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(0));
		
	}
	@Test
	public void testIterateMaxSize() throws InterruptedException, ExecutionException {
		Iterator<Integer> iterator = createInfiniteIterator();
		List<String> strings = new SimpleReact()
				.<Integer> react(iterator ,8)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(8));
		
	}
	@Test
	public void testIterateMaxSize0() throws InterruptedException, ExecutionException {
		Iterator<Integer> iterator = createInfiniteIterator();
		List<String> strings = new SimpleReact()
				.<Integer> react(iterator ,0)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(0));
		
	}
	@Test(expected=IllegalArgumentException.class)
	public void testIterateMaxSizeMinus1() throws InterruptedException, ExecutionException {
		Iterator<Integer> iterator = createInfiniteIterator();
		List<String> strings = new SimpleReact()
				.<Integer> react(iterator ,-1)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		fail("IllegalArgumentException expected");
		
	}
	private Iterator<Integer> createInfiniteIterator() {
		Iterator<Integer> iterator = new Iterator<Integer>(){
			public boolean hasNext(){
				return true;
			}
			public Integer next(){
				return 10;
			}
		};
		return iterator;
	}
}
