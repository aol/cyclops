package com.aol.simple.react.lazy;

import static org.junit.Assert.assertThat;
import static java.util.Arrays.asList;

import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;

import static org.hamcrest.Matchers.*;

import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.traits.FutureStream;

public class LazyTest {

	@Test @Ignore 
	public void debounce() {
		System.out.println(LazyFutureStream.sequentialCommonBuilder()
				.fromPrimitiveStream(IntStream.range(0, 1000000))
				.debounce(100, TimeUnit.MILLISECONDS)
				.peek(System.out::println)
				.block().size());
	}

	@Test @Ignore
	public void skipUntil() {
		FutureStream<Boolean> stoppingStream = LazyFutureStream
				.sequentialBuilder().react(() -> 50).then(this::sleep)
				.peek(System.out::println);
		assertThat(
				LazyFutureStream.sequentialCommonBuilder()
						.fromPrimitiveStream(IntStream.range(0, 100000))
						.skipUntil(stoppingStream).peek(System.out::println)
						.block().size(), greaterThan(0));
	}

	@Test
	@Ignore
	public void takeUntil() {
		FutureStream<Boolean> stoppingStream = LazyFutureStream
				.sequentialBuilder().react(() -> 100).then(this::sleep)
				.peek(System.out::println);
		System.out.println(LazyFutureStream.sequentialCommonBuilder()
				.fromPrimitiveStream(IntStream.range(0, 1000000))
				// .peek(System.out::println)
				.takeUntil(stoppingStream).peek(System.out::println).block()
				.size());
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
		LazyFutureStream.sequentialBuilder().react(() -> 1).map(list -> 1 + 2)
				.block();
	}

	@Test
	public void lazyParallel() {
		LazyFutureStream.parallelBuilder().react(() -> 1).map(list -> 1 + 2)
				.block();
	}

	@Test
	public void lazyReactStreamList() {
		LazyFutureStream.sequentialBuilder().react(asList(() -> 1))
				.map(list -> 1 + 2).block();
	}

	@Test
	public void lazyParallelList() {
		LazyFutureStream.parallelBuilder().react(asList(() -> 1))
				.map(list -> 1 + 2).block();
	}
}
