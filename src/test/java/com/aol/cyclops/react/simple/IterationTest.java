package com.aol.cyclops.react.simple;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

public class IterationTest {

	@Test
	public void testIterate() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList(1,2,3,4);
		List<String> strings = new SimpleReact()
				.<Integer> from(list.iterator())
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
				.<Integer> from(list)
				.peek(it -> System.out.println(it))
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(4));
		
	}
	
	@Test
	public void testReactWithCollectionOfStrings() throws InterruptedException, ExecutionException {
		List<String> list = Arrays.asList("hello","world","$da^","along","$orrupted",null);
		int count  = new SimpleReact()
				.from(list)
				.capture(e -> e.printStackTrace())
				.filter(it -> !it.startsWith("$"))
			
				.onFail(e -> { if(e.getCause() instanceof NullPointerException) { return "null"; } return "";})

				.then(it -> it.length())
				.block().stream().reduce(0, (acc,next) -> acc+next );

		assertThat(count, is(19));
		
	}
	
	@Test
	public void testIterateLargeMaxSize() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList(1,2,3,4);
		List<String> strings = new SimpleReact()
				.<Integer> from(list.iterator())
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(4));
		
	}
	@Test
	public void testIterateEmptyIterator() throws InterruptedException, ExecutionException {
		List<Integer> list = Arrays.asList();
		List<String> strings = new SimpleReact()
				.<Integer> from(list.iterator())
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(0));
		
	}
	@Test @Ignore
	public void testIterateMaxSize() throws InterruptedException, ExecutionException {
		
		
		Iterator<Integer> iterator = createInfiniteIterator();
		List<String> strings = new SimpleReact()
				.<Integer> from(iterator)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(8));
		
	}
	@Test @Ignore
	public void testIterateMaxSize0() throws InterruptedException, ExecutionException {
		Iterator<Integer> iterator = createInfiniteIterator();
		List<String> strings = new SimpleReact()
				.<Integer> from(iterator)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		assertThat(strings.size(), is(0));
		
	}
	/**
	@Test(expected=IllegalArgumentException.class)
	public void testIterateMaxSizeMinus1() throws InterruptedException, ExecutionException {
		Iterator<Integer> iterator = createInfiniteIterator();
		List<String> strings = new SimpleReact()
				.<Integer>of(iterator)
				.then(it -> it * 100)
				.then(it -> "*" + it)
				.block();

		fail("IllegalArgumentException expected");
		
	}**/
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
