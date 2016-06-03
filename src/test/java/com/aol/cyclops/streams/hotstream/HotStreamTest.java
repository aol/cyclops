package com.aol.cyclops.streams.hotstream;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.Queue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.junit.Test;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.stream.PausableHotStream;

public class HotStreamTest {
	static final Executor exec = Executors.newFixedThreadPool(5);
	volatile Object value;
	
	static final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(1);
	String captured;
	long diff;
	@Test
    public void backpressureScheduledDelay(){
       
        captured= "";

           diff =  System.currentTimeMillis();
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(1);
          blockingQueue.add("10");
          blockingQueue.offer("10");
          ReactiveSeq.range(0, Integer.MAX_VALUE)
              .limit(2)
              .peek(v-> diff = System.currentTimeMillis()-diff)
              .map(i -> i.toString())
              .scheduleFixedDelay(1l, scheduled)
              .connect(blockingQueue)
              .onePer(1, TimeUnit.SECONDS)
              .peek(i->System.out.println("BQ " + blockingQueue))
              .peek(System.out::println)
              .forEach(c->captured=c);
        
          assertThat(diff,greaterThan(500l));
    }
	@Test
    public void backpressureScheduledDelayNonBlocking(){
       
        captured= "";

           diff =  System.currentTimeMillis();
          Queue<String> blockingQueue = new ManyToOneConcurrentArrayQueue<String>(1);
         
          ReactiveSeq.range(0, Integer.MAX_VALUE)
              .limit(3)
              .peek(v-> diff = System.currentTimeMillis()-diff)
              .peek(System.out::println)
              .map(i -> i.toString())
              .scheduleFixedDelay(1l, scheduled)
              .connect(blockingQueue)
              .onePer(1, TimeUnit.SECONDS)
              .peek(i->System.out.println("BQ " + blockingQueue))
              .peek(System.out::println)
              .forEach(c->captured=c);
        
          assertThat(diff,lessThan(500l));
    }
	@Test
    public void backpressureScheduledRate(){
       
        captured= "";

           diff =  System.currentTimeMillis();
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(1);
          blockingQueue.add("10");
          blockingQueue.offer("10");
          ReactiveSeq.range(0, Integer.MAX_VALUE)
              .limit(2)
              .peek(v-> diff = System.currentTimeMillis()-diff)
              .map(i -> i.toString())
              .scheduleFixedRate(1l, scheduled)
              .connect(blockingQueue)
              .onePer(1, TimeUnit.SECONDS)
              .peek(i->System.out.println("BQ " + blockingQueue))
              .peek(System.out::println)
              .forEach(c->captured=c);
        
          assertThat(diff,greaterThan(500l));
    }
	@Test
    public void backpressureScheduledCron(){
       
        captured= "";

           diff =  System.currentTimeMillis();
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(1);
          blockingQueue.add("10");
          blockingQueue.offer("10");
          ReactiveSeq.range(0, Integer.MAX_VALUE)
              .limit(2)
              .peek(v-> diff = System.currentTimeMillis()-diff)
              .map(i -> i.toString())
              .schedule("* * * * * ?", scheduled)
              .connect(blockingQueue)
              .onePer(2, TimeUnit.SECONDS)
              .peek(i->System.out.println("BQ " + blockingQueue))
              .peek(System.out::println)
              .forEach(c->captured=c);
        
          assertThat(diff,greaterThan(1500l));
    }
	@Test
	public void backpressurePrimed(){
	   
	    captured= "";

	      Executor exec = Executors.newFixedThreadPool(1);
	      LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(1);
	      diff =  System.currentTimeMillis();
	      ReactiveSeq.range(0, Integer.MAX_VALUE)
	          .limit(2)
	          .map(i -> i.toString())
	          .peek(v-> diff = System.currentTimeMillis()-diff)
	          .peek(System.out::println)
	          .primedHotStream(exec)
	          .connect(blockingQueue)
	          .onePer(1, TimeUnit.SECONDS)
	          .forEach(c->captured=c);
	    
	     
	      assertThat(diff,greaterThan(500l));
	}
	@Test
    public void backpressure(){
        captured= "";

          
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(3);
          diff =  System.currentTimeMillis();
          ReactiveSeq.range(0, Integer.MAX_VALUE)
              .limit(2)
              .map(i -> i.toString())
              .peek(v-> diff = System.currentTimeMillis()-diff)
              .peek(System.out::println)
              .hotStream(exec)
              .connect(blockingQueue)
              .onePer(1, TimeUnit.SECONDS)
              .forEach(c->captured=c);
        
          assertThat(diff,greaterThan(500l));
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
