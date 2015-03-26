package com.aol.simple.react.eager;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.stream.IntStream;

import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerFutureStream;

public class EagerTest {

	@Test @Ignore
	public void jitter(){
		EagerFutureStream.parallelCommonBuilder()
						.fromPrimitiveStream(IntStream.range(0, 1000000))
						.map(it -> it*100)
						.jitter(10l)
						.peek(System.out::println)
						.block();
	}
	@Test @Ignore
	public void jitterSequential(){
		EagerFutureStream.sequentialCommonBuilder()
						.fromPrimitiveStream(IntStream.range(0, 1000000))
						.map(it -> it*100)
						.jitter(100000l)
						.peek(System.out::println)
						.runOnCurrent();
	}
	@Test
	public void doOnEach(){
		String[] found = {""};
		String res = EagerFutureStream.sequentialBuilder().react(()->"hello").doOnEach(it->{ found[0]=it;return "world";}).map(it->it+"!").first();
		assertThat(found[0],is("hello"));
		assertThat(res,is("hello!"));
	}
	
	@Test
	public void eagerReactStream(){
		EagerFutureStream.sequentialBuilder()
			.react( ()-> 1 )
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void eagerParallel(){
		EagerFutureStream.parallelBuilder()
			.react( ()-> 1 )
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void eagerReactStreamList(){
		EagerFutureStream.sequentialBuilder()
			.react( asList(()-> 1 ))
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void eagerParallelList(){
		EagerFutureStream.parallelBuilder()
			.react( asList(()-> 1 ))
			.map(list -> 1+2)
			.block();
	}
}
