package com.aol.simple.react.eager;

import static java.util.Arrays.asList;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerFutureStream;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class EagerTest {

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
