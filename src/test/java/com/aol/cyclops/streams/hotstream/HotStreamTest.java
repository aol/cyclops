package com.aol.cyclops.streams.hotstream;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Spliterator;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.StreamSupport;

import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.PausableHotStream;

public class HotStreamTest {
	static final Executor exec = Executors.newFixedThreadPool(5);
	volatile Object value;
	
	String captured;
	@Test
	public void backpressurePrimed(){
	   
	    captured= "";

	      Executor exec = Executors.newFixedThreadPool(1);
	      LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(3);

	      ReactiveSeq.range(0, Integer.MAX_VALUE)
	          .limit(2)
	          .map(i -> i.toString())
	          .peek(System.out::println)
	          .primedHotStream(exec)
	          .connect(blockingQueue)
	          .onePer(1, TimeUnit.SECONDS)
	          .forEach(c->captured=c);
	    
	      assertThat(captured,equalTo("1"));
	}
	@Test
    public void backpressure(){
        captured= "";

          Executor exec = Executors.newFixedThreadPool(1);
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(3);

          ReactiveSeq.range(0, Integer.MAX_VALUE)
              .limit(2)
              .map(i -> i.toString())
              .peek(System.out::println)
              .hotStream(exec)
              .connect(blockingQueue)
              .onePer(1, TimeUnit.SECONDS)
              .forEach(c->captured=c);
        
          assertThat(captured,equalTo("1"));
    }
	@Test
	public void hotStream() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		ReactiveSeq.of(1,2,3)
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
			ReactiveSeq.range(0,Integer.MAX_VALUE)
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
		ReactiveSeq.range(0,Integer.MAX_VALUE)
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
		active=true;
		CountDownLatch latch = new CountDownLatch(1);
		PausableHotStream s = ReactiveSeq.range(0,Integer.MAX_VALUE)
		        .limitWhile(i->active)
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
		while(value==null)
            Thread.sleep(1000);
		s.pause();
		System.out.println(value);
		assertTrue(value!=oldValue);
		s.unpause();
		latch.await();
		assertTrue(value!=null);
		active=false;
	}
	volatile boolean active;
	@Test
	public void hotStreamConnectPausableConnect() throws InterruptedException{
		value= null;
		active=true;
		CountDownLatch latch = new CountDownLatch(1);
		PausableHotStream s = ReactiveSeq.range(0,Integer.MAX_VALUE)
		        .limitWhile(i->active)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.pausableHotStream(exec);
		Object oldValue = value;
		s.connect()
				.limit(10000)
				.futureOperations(ForkJoinPool.commonPool())
				.forEach(System.out::println);
		
		
		s.pause();
		s.unpause();

		while(value==null)
		    Thread.sleep(1000);
		s.pause();
		assertTrue("value= " +  value,value!=oldValue);
		s.unpause();
		latch.await();
		assertTrue(value!=null);
		active=false;
	}
}
