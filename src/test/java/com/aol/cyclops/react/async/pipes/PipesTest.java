package com.aol.cyclops.react.async.pipes;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.concurrent.ForkJoinPool;

import org.junit.Before;
import org.junit.Test;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.types.stream.reactive.SeqSubscriber;
public class PipesTest {
	@Before
	public void setup() {
		Pipes.clear();
	}
	@Test
	public void testGetAbsent() {
		
		assertFalse(Pipes.get("hello").isPresent());
	}
	@Test
	public void testGetPresent() {
		Pipes.register("hello", new Queue());
		assertTrue(Pipes.get("hello").isPresent());
	}

	@Test
	public void publisherAbsent(){
		assertFalse(Pipes.publisher("hello",ForkJoinPool.commonPool()).isPresent());
	}
	@Test
	public void publisherPresent(){
		Pipes.register("hello", new Queue());
		assertTrue(Pipes.publisher("hello").isPresent());
	}
	@Test
	public void publisherTest(){
		SeqSubscriber subscriber = SeqSubscriber.subscriber();
		Queue queue = new Queue();
		Pipes.register("hello", queue);
		Pipes.publisher("hello",ForkJoinPool.commonPool()).get().subscribe(subscriber);
		queue.offer("world");
		queue.close();
		assertThat(subscriber.stream().findAny().get(),equalTo("world"));
	}
	@Test
	public void subscribeTo(){
	    SeqSubscriber subscriber = SeqSubscriber.subscriber();
		Queue queue = new Queue();
		Pipes.register("hello", queue);
		Pipes.subscribeTo("hello",subscriber,ForkJoinPool.commonPool());
		queue.offer("world");
		queue.close();
		assertThat(subscriber.stream().findAny().get(),equalTo("world"));
	}
	@Test
	public void publishTo() throws InterruptedException{
		
		Queue queue = new Queue();
		Pipes.register("hello", queue);
		Pipes.publishToAsync("hello",LazyFutureStream.of(1,2,3));
		Thread.sleep(100);
		queue.offer(4);
		queue.close();
		assertThat(queue.stream().toList(),equalTo(Arrays.asList(1,2,3,4)));
	}
}

