package com.aol.cyclops2.streams.hotstream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.LockSupport;

import org.junit.Test;

import cyclops.stream.ReactiveSeq;
import com.aol.cyclops2.types.stream.PausableHotStream;

public class PrimedHotStreamTest {
	static final Executor exec = Executors.newFixedThreadPool(1);
	volatile Object value;
	
	@Test
	public void hotStream() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		ReactiveSeq.of(1,2,3)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.primedHotStream(exec)
				.connect().forEach(System.out::println);
		
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
			ReactiveSeq.range(0,Integer.MAX_VALUE)
					.limit(100)
					.peek(v->value=v)
					.peek(v->latch.countDown())
					.peek(System.out::println)
					.primedHotStream(exec)
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
		ReactiveSeq.range(0,Integer.MAX_VALUE)
				.limit(1000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.primedHotStream(exec)
				.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(),s->s.forEach(System.out::println));
		
		latch.await();
		assertTrue(value!=null);
	}
	@Test
	public void hotStreamCapture() throws InterruptedException{
		
		
		List<Integer> list = ReactiveSeq.range(0,Integer.MAX_VALUE)
									 .limit(1000)
									 .primedHotStream(exec)
									 .connect()
									 .limit(2)
									 .toList();
		
		assertThat(list,equalTo(Arrays.asList(0,1)));
		
	}
	volatile boolean active;
	@Test
	public void hotStreamConnectPausable() throws InterruptedException{
		value= null;
		active=true;
		CountDownLatch latch = new CountDownLatch(1);
		PausableHotStream<Integer> s = ReactiveSeq.range(0,Integer.MAX_VALUE)
				.limitWhile(i->active)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.primedPausableHotStream(exec);
		s.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(),st->st.forEach(System.out::println));
		
		Object oldValue = value;
	
		
		try{
			s.pause();
			s.unpause();
			Thread.sleep(1000);
			s.pause();
			assertTrue(value!=oldValue);
			s.unpause();
			latch.await();
			assertTrue(value!=null);
		}finally{
		    active=false;
			s.unpause();
		}
	}
	@Test
	public void hotStreamConnectPausableConnect() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		PausableHotStream<Integer> s = ReactiveSeq.range(0,Integer.MAX_VALUE)
				.limit(50000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.primedPausableHotStream(exec);
		s.connect()
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(),st->st.forEach(System.out::println));

		
		Object oldValue = value;
		try{
			s.pause();
			s.unpause();
			LockSupport.parkNanos(1000l);
			s.pause();
			System.out.println(value);
			assertTrue("value =" + value!=oldValue);
			s.unpause();
			latch.await();
			assertTrue(value!=null);
		}finally{
			s.unpause();
		}
	}
}
