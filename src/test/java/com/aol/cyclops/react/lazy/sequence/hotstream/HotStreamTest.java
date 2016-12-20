package com.aol.cyclops.react.lazy.sequence.hotstream;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;

import org.junit.Test;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.types.futurestream.LazyFutureStream;



public class HotStreamTest {
	static final Executor exec = Executors.newFixedThreadPool(1);
	volatile Object value;
	@Test
	public void hotStream() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		
		LazyFutureStream.of(1,2,3)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.hotStream(exec);
		
		latch.await();
		assertTrue(value!=null);
	}
	@Test
	public void hotStreamOwn() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		LazyFutureStream.of(1,2,3)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.hotStream();
		
		latch.await();
		assertTrue(value!=null);
	}
	@Test
	public void hotStreamConnect() throws InterruptedException{
		
		
		for(int i=0;i<1_000;i++)
		{
			System.out.println(i);
			value= null;
			CountDownLatch latch = new CountDownLatch(1);
			new LazyReact().range(0,Integer.MAX_VALUE)
					.limit(100)
					.peek(v->value=v)
					.peek(v->latch.countDown())
					.peek(System.out::println)
					.hotStream(exec)
					.connect()
					.limit(100)
					.runFuture(ForkJoinPool.commonPool(),s->s.forEach(System.out::println));
			
			latch.await();
			assertTrue(value!=null);
		}
	}
	
	@Test
	public void hotStreamConnectBlockingQueue() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		new LazyReact().range(0,Integer.MAX_VALUE)
				.limit(1000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.hotStream(exec)
				.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(),s->s.forEach(System.out::println));
		
		latch.await();
		assertTrue(value!=null);
	}
}
