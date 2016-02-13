package com.aol.cyclops.react.async.pipes;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.react.async.Queue;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;

public class LazyPipesTest {
	@Before
	public void setup() {
		Pipes.clear();
	}
	
	@Test
	public void testStream() {
		Queue queue = new Queue();
		queue.add("world");
		Pipes.register("hello",queue);
		assertThat(PipesToLazyStreams.stream("hello").limit(1).toList(),equalTo(Arrays.asList("world")));
	}
	@Test
	public void testStreamIO() {
		Queue queue = new Queue();
		queue.add("world");
		Pipes.register("hello",queue);
		assertThat(PipesToLazyStreams.streamIOBound("hello").limit(1).toList(),equalTo(Arrays.asList("world")));

	
	
	}
	@Test
	public void testStreamIOTime() {
		Queue queue = new Queue();
		queue.add("world");
		Pipes.register("hello",queue);
		assertThat(PipesToLazyStreams.streamIOBound("hello").limit(1, TimeUnit.SECONDS).toList(),equalTo(Arrays.asList("world")));

	
	
	}
	@Test
	public void testStreamCPU() {
		Queue queue = new Queue();
		queue.add("world");
		Pipes.register("hello",queue);
		assertThat(PipesToLazyStreams.streamCPUBound("hello").limit(1).toList(),equalTo(Arrays.asList("world")));
	}
	@Test
	public void cpuBound() {
		Queue queue = new Queue();
		LazyFutureStream<String> stream = PipesToLazyStreams.registerForCPU("hello", queue);
		queue.add("world");
		assertTrue(Pipes.get("hello").isPresent());
		assertThat(stream.limit(1).toList(),equalTo(Arrays.asList("world")));
	}
	@Test
	public void ioBound() {
		Queue queue = new Queue();
		LazyFutureStream<String> stream = PipesToLazyStreams.registerForIO("hello", queue);
		queue.add("world");
		assertTrue(Pipes.get("hello").isPresent());
		assertThat(stream.limit(1).toList(),equalTo(Arrays.asList("world")));
	}
}

