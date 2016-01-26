package com.aol.cyclops.streams.hotstream;

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.IntStream;

import org.junit.Test;

import uk.co.real_logic.agrona.concurrent.OneToOneConcurrentArrayQueue;

import com.aol.cyclops.sequence.PausableHotStream;
import com.aol.cyclops.sequence.SequenceM;

import fj.data.Seq;

public class HotStreamTest {
	static final Executor exec = Executors.newFixedThreadPool(1);
	volatile Object value;
	
	@Test
	public void hotStream() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		SequenceM.of(1,2,3)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.hotStream(exec);
		
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
			SequenceM.range(0,Integer.MAX_VALUE)
					.limit(100)
					.peek(v->value=v)
					.peek(v->latch.countDown())
					.peek(System.out::println)
					.hotStream(exec)
					.connect()
					.limit(100)
					.futureOperations(ForkJoinPool.commonPool())
					.forEach(System.out::println);
			
			latch.await();
			assertTrue(value!=null);
		}
	}
	
	@Test
	public void hotStreamConnectBlockingQueue() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		SequenceM.range(0,Integer.MAX_VALUE)
				.limit(1000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.hotStream(exec)
				.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.futureOperations(ForkJoinPool.commonPool())
				.forEach(System.out::println);
		
		latch.await();
		assertTrue(value!=null);
	}
	@Test
	public void hotStreamConnectPausable() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		PausableHotStream s = SequenceM.range(0,Integer.MAX_VALUE)
				.limit(1000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.pausableHotStream(exec);
		s.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.futureOperations(ForkJoinPool.commonPool())
				.forEach(System.out::println);
		
		Object oldValue = value;
		s.pause();
		s.unpause();
		LockSupport.parkNanos(1000l);
		s.pause();
		System.out.println(value);
		assertTrue(value!=oldValue);
		s.unpause();
		latch.await();
		assertTrue(value!=null);
	}
	@Test
	public void hotStreamConnectPausableConnect() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		PausableHotStream s = SequenceM.range(0,Integer.MAX_VALUE)
				.limit(10000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.pausableHotStream(exec);
		s.connect()
				.limit(100)
				.futureOperations(ForkJoinPool.commonPool())
				.forEach(System.out::println);
		
		Object oldValue = value;
		s.pause();
		s.unpause();
		LockSupport.parkNanos(1000l);
		s.pause();
		System.out.println(value);
		assertTrue(value!=oldValue);
		s.unpause();
		latch.await();
		assertTrue(value!=null);
	}
}
