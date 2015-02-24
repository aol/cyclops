package com.aol.simple.react.eager;

import static java.util.Arrays.asList;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerFutureStream;

public class EagerTest {

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
