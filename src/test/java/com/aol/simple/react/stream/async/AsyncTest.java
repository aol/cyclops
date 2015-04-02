package com.aol.simple.react.stream.async;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerFutureStream;
import com.aol.simple.react.stream.lazy.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public class AsyncTest {

	@Test
	public void testSequentialLazy(){
		assertThat(LazyFutureStream.sequentialBuilder().isAsync(),is(false));
	}
	@Test
	public void testSequentialCommonLazy(){
		assertThat(LazyFutureStream.sequentialCommonBuilder().isAsync(),is(false));
	}
	@Test
	public void testSequentialEager(){
		assertThat(EagerFutureStream.sequentialBuilder().isAsync(),is(false));
	}
	@Test
	public void testSequentialCommonEager(){
		assertThat(EagerFutureStream.sequentialCommonBuilder().isAsync(),is(false));
	}
	@Test
	public void testSequentialSimple(){
		assertThat(SimpleReactStream.sequentialBuilder().isAsync(),is(false));
	}
	@Test
	public void testSequentialCommonSimple(){
		assertThat(SimpleReactStream.sequentialCommonBuilder().isAsync(),is(false));
	}
	@Test
	public void testParallelLazy(){
		assertThat(LazyFutureStream.parallelBuilder().isAsync(),is(true));
	}
	@Test
	public void testParallelLazyInt(){
		assertThat(LazyFutureStream.parallelBuilder(3).isAsync(),is(true));
	}
	@Test
	public void testParallelCommonLazy(){
		assertThat(LazyFutureStream.parallelCommonBuilder().isAsync(),is(true));
	}
	@Test
	public void testParallelEager(){
		assertThat(EagerFutureStream.parallelBuilder().isAsync(),is(true));
	}
	@Test
	public void testParallelEagerInt(){
		assertThat(EagerFutureStream.parallelBuilder(2).isAsync(),is(true));
	}
	@Test
	public void testParallelCommonEager(){
		assertThat(EagerFutureStream.parallelCommonBuilder().isAsync(),is(true));
	}
	@Test
	public void testParallelSimple(){
		assertThat(SimpleReactStream.parallelBuilder().isAsync(),is(true));
	}
	@Test
	public void testParallelSimpleInt(){
		assertThat(SimpleReactStream.parallelBuilder(2).isAsync(),is(true));
	}
	@Test
	public void testParallelCommonSimple(){
		assertThat(SimpleReactStream.parallelCommonBuilder().isAsync(),is(true));
	}
}
