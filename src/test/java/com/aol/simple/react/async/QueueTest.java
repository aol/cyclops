package com.aol.simple.react.async;

import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.aol.simple.react.SimpleReact;

public class QueueTest {

	@Before
	public void setup(){
		found =0;
	}
	int found =0;
	public synchronized void incrementFound(){
		found++;
	}
	
	@Test
	public void enqueueTest(){
		Stream<String> stream  = Stream.of("1","2","3");
		Queue<String> q = new Queue(new LinkedBlockingQueue());
		q.fromStream(stream);
		Stream<String> dq = q.stream();
	
		
		Integer dequeued = q.stream().limit(3).map(it -> Integer.valueOf(it)).reduce(0,(acc,next) -> acc+next);
		
		assertThat(dequeued,is(6));
	}
	volatile	int  count =0;
	volatile	int  count1 =10000;
	
	@Test 
	public void simpleMergingTestLazyIndividualMerge(){
		Queue<Integer> q = new Queue(new LinkedBlockingQueue());
		q.add(0);
		q.add(100000);
		
		List<Integer> result = q.stream().limit(2).peek(it->System.out.println(it)).collect(Collectors.toList());
		assertThat(result,hasItem(100000));
		assertThat(result,hasItem(0));
	
	}
	
	@Test @Ignore //too non-deterministic to run regularly - relying on population from competing threads
	public void mergingTestLazyIndividualMerge(){
		count = 0;
		count1 = 100000;
		
		Queue<Integer> q = new Queue(new LinkedBlockingQueue());
		SimpleReact.lazy().reactInfinitely(() ->  count++).then(it->q.add(it)).run(new ForkJoinPool(1));
		SimpleReact.lazy().reactInfinitely(() -> count1++).then(it->q.add(it)).run(new ForkJoinPool(1));
		
		
		
		List<Integer> result = q.stream().limit(1000).peek(it->System.out.println(it)).collect(Collectors.toList());
		assertThat(result,hasItem(100000));
		assertThat(result,hasItem(0));
	
	}
	
	@Test 
	public void simpleMergingTestEagerStreamMerge(){
		
	
		Queue<Integer> q = new Queue(new LinkedBlockingQueue());

		q.add(0);
		q.add(100000);
		
		
		
		
		List<Integer> result = q.stream().limit(2).peek(it->System.out.println(it)).collect(Collectors.toList());
		assertThat(result,hasItem(100000));
		assertThat(result,hasItem(0));
	
	}
	
	
	@Test @Ignore //too non-deterministic to run regularly - relying on population from competing threads
	public void mergingTestEagerStreamMerge(){
		count = 0;
		count1 = 100000;
		
		
		Queue<Integer> q = new Queue(new LinkedBlockingQueue());

		new SimpleReact().react(()-> q.fromStream(Stream.generate(()->count++)));
		new SimpleReact().react(()-> q.fromStream(Stream.generate(()->count1++)));

		
		
		
		List<Integer> result = q.stream().limit(1000).peek(it->System.out.println(it)).collect(Collectors.toList());
		assertThat(result,hasItem(100000));
		assertThat(result,hasItem(0));
	
	}
	
	
	@Test(expected=Queue.ClosedQueueException.class)
	public void queueTestBlock(){
		
		try{
			Queue q = new Queue<>(new LinkedBlockingQueue<>());
			
			
			new SimpleReact().react(() -> q.add(1), ()-> q.add(2),()-> {sleep(50); return q.add(4); }, ()-> { sleep(400); q.close(); return 1;});
			
			
			
			SimpleReact.lazy().fromStream(q.streamCompletableFutures())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.block();
			
			
				
		}finally{
			assertThat(found,is(3));
		}
		
		
	}
	@Test
	public void queueTestTimeout(){
		
		
			Queue q = new Queue<>(new LinkedBlockingQueue<>()).withTimeout(1).withTimeUnit(TimeUnit.MILLISECONDS);
			
			
			new SimpleReact().react(() -> q.add(1), ()-> q.add(2),()-> {sleep(500); return q.add(4); }, ()-> q.add(5));
			
			
			
			Collection<String> results = SimpleReact.lazy().fromStream(q.streamCompletableFutures())
					.then(it -> "*" +it)
					.run(() -> new ArrayList<String>());
		
			assertThat(results.size(),is(3));
			assertThat(results,not(hasItem("*4")));
			assertThat(results,hasItem("*5"));
			
				
	
		
		
	}
	@Test
	public void queueTestRun(){
		try{
			Queue<Integer> q = new Queue<>(new LinkedBlockingQueue<>());
						
			new SimpleReact().react(() -> q.add(1), ()-> q.add(2),()-> {sleep(200); return q.add(4); }, ()-> { sleep(400); q.close(); return 1;});
			
			
			List<String> result = SimpleReact.lazy().fromStream(q.streamCompletableFutures())
					.then(it -> "*" +it)
					.peek(it -> incrementFound())
					.peek(it -> System.out.println(it))
					.run(() -> new ArrayList<String>());
			
			assertThat(result,hasItem("*1"));
				
		}finally{
			assertThat(found,is(3));
		}
		
		
	}

	private int sleep(int i) {
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return i;
		
	}
}
