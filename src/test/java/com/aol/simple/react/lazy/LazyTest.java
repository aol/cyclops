package com.aol.simple.react.lazy;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Test;

import com.aol.simple.react.stream.lazy.LazyReact;

public class LazyTest {

	int slow(){
		try {
			Thread.sleep(150);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 3;
	}
	
	@Test
	public void convertToEager(){
		
		
		
		
		assertThat(LazyReact.parallelCommonBuilder()
						.react(()->slow(),()->1,()->2)
						.peek(System.out::println)
						.convertToSimpleReact()
						.allOf(list->list)
						.block().size(),is(1));
						
	}

	@Test
	public void convertToEagerAndBack(){
		
		
		
		
		assertThat(LazyReact.parallelCommonBuilder()
						.react(()->slow(),()->1,()->2)
						.peek(System.out::println)
						.zipWithIndex()
						.convertToSimpleReact()
						.peek(System.out::println)
						.convertToLazyStream()
						.map(it->slow())
						.peek(System.out::println)
						.block().size(),is(3));
						
	}
	
	@Test
	public void zipWithIndexApi(){
		LazyReact.parallelCommonBuilder()
		.react(() -> 2, () -> 1, () -> 2)
		
		.zipWithIndex()
		.peek(System.out::println)
		.map(it -> {
			if (it.v1 == 1) {
				sleep(1000);
				return -1;
			}
			return it.v1 + 100;
		})
		.peek(System.out::println)
		.forEach(System.out::println);
	}
	@Test 
	public void debounce() {
		System.out.println(LazyReact.sequentialCommonBuilder()
				.from(IntStream.range(0, 1000000))
				.limit(100)
				.debounce(100, TimeUnit.MILLISECONDS)
				.peek(System.out::println)
				.block().size());
	}

	

	

	private boolean sleep(int i) {

		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
		return true;

	}

	@Test
	public void lazyReactStream() {
		assertThat(LazyReact.sequentialBuilder().react(() -> 1).map(list -> 1 + 2)
				.block(),equalTo(Arrays.asList(3)));
	}
	@Test
	public void lazyReactParAndConc() {
		assertThat(new LazyReact(2,2).react(() -> 1).map(list -> 1 + 2)
				.block(),equalTo(Arrays.asList(3)));
	}

	@Test
	public void lazyParallel() {
		assertThat(LazyReact.parallelBuilder().react(() -> 1).map(list -> 1 + 2)
				.block(),equalTo(Arrays.asList(3)));
	}

	@Test
	public void lazyReactStreamList() {
		assertThat(LazyReact.sequentialBuilder().react(asList(() -> 1))
				.map(list -> 1 + 2).block(),equalTo(Arrays.asList(3)));
	}

	@Test
	public void lazyParallelList() {
		assertThat(LazyReact.parallelBuilder().react(asList(() -> 1))
				.map(list -> 1 + 2).block(),equalTo(Arrays.asList(3)));
	}
}
