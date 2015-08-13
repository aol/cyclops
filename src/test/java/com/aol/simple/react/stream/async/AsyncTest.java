package com.aol.simple.react.stream.async;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import org.junit.Test;

import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;

public class AsyncTest {

	@Test
	public void testSequentialLazy(){
		assertThat(LazyReact.sequentialBuilder().isAsync(),is(false));
	}
	@Test
	public void testSequentialCommonLazy(){
		assertThat(LazyReact.sequentialCommonBuilder().isAsync(),is(false));
	}
	@Test
	public void testSequentialEager(){
		assertThat(EagerReact.sequentialBuilder().isAsync(),is(false));
	}
	@Test
	public void testSequentialCommonEager(){
		assertThat(EagerReact.sequentialCommonBuilder().isAsync(),is(false));
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
		assertThat(LazyReact.parallelBuilder().isAsync(),is(true));
	}
	@Test
	public void testParallelLazyInt(){
		assertThat(LazyReact.parallelBuilder(3).isAsync(),is(true));
	}
	@Test
	public void testParallelCommonLazy(){
		assertThat(LazyReact.parallelCommonBuilder().isAsync(),is(true));
	}
	@Test
	public void testParallelEager(){
		assertThat(EagerReact.parallelBuilder().isAsync(),is(true));
	}
	@Test
	public void testParallelEagerInt(){
		assertThat(EagerReact.parallelBuilder(2).isAsync(),is(true));
	}
	@Test
	public void testParallelCommonEager(){
		assertThat(EagerReact.parallelCommonBuilder().isAsync(),is(true));
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
