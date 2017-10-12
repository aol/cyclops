package cyclops.streams.push.async;

import com.aol.cyclops2.types.stream.PausableHotStream;
import cyclops.reactive.ReactiveSeq;
import cyclops.reactive.Spouts;
import org.agrona.concurrent.ManyToOneConcurrentArrayQueue;
import org.junit.Ignore;
import org.junit.Test;

import java.util.Queue;
import java.util.concurrent.*;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class AsyncHotStreamTest {
	static final Executor exec = Executors.newFixedThreadPool(15);
	static final Executor exec2 = Executors.newFixedThreadPool(5);
	
	volatile Object value;
	
	static final ScheduledExecutorService scheduled = Executors.newScheduledThreadPool(15);
	String captured;
	long diff;
	protected <U> ReactiveSeq<U> of(U... array){

		return Spouts.async(s->{
			Thread t = new Thread(()-> {
				for (U next : array) {
					s.onNext(next);
				}
				s.onComplete();
			});
			t.start();
		});
	}
	protected  ReactiveSeq<Integer> range(int start,int end){

		return Spouts.async(s->{
			Thread t = new Thread(()-> {
				range(start,end).forEach(s::onNext);
					
				s.onComplete();
			});
			t.start();
		});
	}
	@Test @Ignore
    public void backpressureScheduledDelay(){
       
        captured= "";

           diff =  System.currentTimeMillis();
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(1);
          blockingQueue.add("10");
          blockingQueue.offer("10");
          range(0, Integer.MAX_VALUE)
              .limit(2)
              .peek(v-> diff = System.currentTimeMillis())
              .peek(i->System.out.println("diff is "  +diff))
              .map(i -> i.toString())
              .scheduleFixedDelay(1l, scheduled)
              .connect(blockingQueue)
              .onePer(1, TimeUnit.SECONDS)
              .peek(i->System.out.println("BQ " + blockingQueue))
              .peek(System.out::println)
              .forEach(c->captured=c);
        
          assertThat(System.currentTimeMillis() - diff,greaterThan(1500l));
    }
	@Test @Ignore
    public void backpressureScheduledDelayNonBlocking(){
       
        captured= "";

           diff =  System.currentTimeMillis();
          Queue<String> blockingQueue = new ManyToOneConcurrentArrayQueue<String>(1);
         
        
          range(0, Integer.MAX_VALUE)
              .limit(3)
              .peek(i->System.out.println("diff before is "  +diff))
              .peek(v-> diff = System.currentTimeMillis()-diff)
              .peek(i->System.out.println("diff is "  +diff))
              .map(i -> i.toString())
              .scheduleFixedDelay(1l, scheduled)
              .connect(blockingQueue)
              .onePer(1, TimeUnit.SECONDS)
              .peek(i->System.out.println("BQ " + blockingQueue))
              .peek(System.out::println)
              .forEach(c->captured=c);
        
          assertThat(diff,lessThan(500l));
    }
	@Test @Ignore
    public void backpressureScheduledRate(){
       
        captured= "";

           diff =  System.currentTimeMillis();
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(1);
          blockingQueue.add("10");
          blockingQueue.offer("10");
          range(0, Integer.MAX_VALUE)
              .limit(2)
              .peek(v-> diff = System.currentTimeMillis())
              .map(i -> i.toString())
              .scheduleFixedRate(1l, scheduled)
              .connect(blockingQueue)
              .onePer(1, TimeUnit.SECONDS)
              .peek(i->System.out.println("BQ " + blockingQueue))
              .peek(System.out::println)
              .forEach(c->captured=c);
        
          assertThat(System.currentTimeMillis() - diff,greaterThan(1500l));
    }
	@Test @Ignore
    public void backpressureScheduledCron(){
       
        captured= "";

           diff =  System.currentTimeMillis();
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(1);
          blockingQueue.add("10");
          blockingQueue.offer("10");
          range(0, Integer.MAX_VALUE)
              .limit(2)
              .peek(v-> diff = System.currentTimeMillis())
              .map(i -> i.toString())
              .schedule("* * * * * ?", scheduled)
              .connect(blockingQueue)
              .onePer(2, TimeUnit.SECONDS)
              .peek(i->System.out.println("BQ " + blockingQueue))
              .peek(System.out::println)
              .forEach(c->captured=c);
        
          assertThat(System.currentTimeMillis() - diff,greaterThan(1500l));
    }
	@Test @Ignore
	public void backpressurePrimed(){
	   
	    captured= "";

	      Executor exec = Executors.newFixedThreadPool(1);
	      LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(1);
	      diff =  System.currentTimeMillis();
	      range(0, Integer.MAX_VALUE)
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
	@Test @Ignore
    public void backpressure(){
        captured= "";

          
          LinkedBlockingQueue<String> blockingQueue = new LinkedBlockingQueue<String>(3);
          diff =  System.currentTimeMillis();
          range(0, Integer.MAX_VALUE)
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
		of(1,2,3)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.hotStream(exec);
		
		latch.await();
		assertTrue(value!=null);
	}
	@Test @Ignore
	public void hotStreamConnect() throws InterruptedException{
		
		
		for(int i=0;i<1_000;i++)
		{
			System.out.println(i);
			value= null;
			CountDownLatch latch = new CountDownLatch(1);
			range(0,Integer.MAX_VALUE)
					.limit(100)
					.peek(v->value=v)
					.peek(v->latch.countDown())
					.peek(System.out::println)
					.hotStream(exec)
					.connect()
					.limit(100)
					.runFuture(ForkJoinPool.commonPool(),t->
					   t.forEach(System.out::println,System.err::println));
			
			latch.await();
			assertTrue(value!=null);
		}
	}
	
	@Test @Ignore
	public void hotStreamConnectBlockingQueue() throws InterruptedException{
		value= null;
		CountDownLatch latch = new CountDownLatch(1);
		range(0,Integer.MAX_VALUE)
				.limit(1000)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.hotStream(exec)
				.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(),t ->
				   t.forEach(System.out::println,System.err::println));
		
		latch.await();
		assertTrue(value!=null);
	}
	@Test @Ignore
	public void hotStreamConnectPausable() throws InterruptedException{
	    Thread.sleep(1000l);
		value= null;
		active=true;
		CountDownLatch latch = new CountDownLatch(1);
		PausableHotStream<Integer> s = range(0,Integer.MAX_VALUE)
		        .limitWhile(i->active)
				.peek(v->value=v)
				.peek(v->latch.countDown())
				.pausableHotStream(exec2);
		s.connect(new LinkedBlockingQueue<>())
				.limit(100)
				.runFuture(ForkJoinPool.commonPool(), ft ->
						ft.forEach(System.out::println,System.err::println));
		
		Object oldValue = value;
		s.pause();
		s.unpause();
		while(value==null)
            Thread.sleep(1000);
		s.pause();
		System.out.println(value);
		assertTrue("value is "  + value + ". oldValue is " + oldValue,value!=oldValue);
		s.unpause();
		latch.await();
		assertTrue(value!=null);
		active=false;
	}
	volatile boolean active;
}
