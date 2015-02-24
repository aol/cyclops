package com.aol.simple.react.lazy;

import static java.util.Arrays.asList;

import java.util.Arrays;

import org.junit.Test;

import com.aol.simple.react.stream.lazy.LazyFutureStream;

public class LazyTest {
	@Test
	public void lazyReactStream(){
		LazyFutureStream.sequentialBuilder()
			.react( ()-> 1 )
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void lazyParallel(){
		LazyFutureStream.parallelBuilder()
			.react( ()-> 1 )
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void lazyReactStreamList(){
		LazyFutureStream.sequentialBuilder()
			.react( asList(()-> 1 ))
			.map(list -> 1+2)
			.block();
	}
	@Test
	public void lazyParallelList(){
		LazyFutureStream.parallelBuilder()
			.react( asList(()-> 1 ))
			.map(list -> 1+2)
			.block();
	}
}
