package com.aol.cyclops.util.stream.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.types.futurestream.LazyFutureStream;
import com.aol.cyclops.util.stream.reactivestreams.FutureStreamSubscriber;

public class ErrorTest {
	@Test
	public void testSubscribe(){
		FutureStreamSubscriber<Integer> sub = new FutureStreamSubscriber();
		
		
		LazyFutureStream.of(1,2,3).subscribe(sub);
		
		
		sub.getStream().forEach(System.out::println);
	}
	@Test
	public void testProcessorSubscribe(){
		FutureStreamProcessor<Integer,Integer> sub = new FutureStreamProcessor();
		
		
		LazyFutureStream.of(1,2,3).subscribe(sub);
		sub.getSubscription().request(5);
		
		
		assertThat(sub.getStream().toList(),equalTo(Arrays.asList(1,2,3)));
	}
	List<Integer> result;
	@Test @Ignore
	public void testProcessorPublish() throws InterruptedException{
		
		result = new ArrayList<>();
		FutureStreamProcessor<Integer,Integer> pub = new FutureStreamProcessor();
		
		Subscriber subscriber =new Subscriber<Integer>() {

			@Override
			public void onSubscribe(Subscription s) {
				s.request(5);
				
			}

			@Override
			public void onNext(Integer t) {
				result.add(t);
				
			}

			@Override
			public void onError(Throwable t) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		};
		pub.subscribe(subscriber);
		pub.onSubscribe(new Subscription() {
			
			@Override
			public void request(long n) {
				for(int i=1;i<n;i++)
					subscriber.onNext(i);
				subscriber.onComplete();
				
			}
			
			@Override
			public void cancel() {
				// TODO Auto-generated method stub
				
			}
		});
		Thread.sleep(100);
		
		System.out.println("result " + result);
		assertThat(result,equalTo(Arrays.asList(1,2,3,4)));
		
	}
	@Test @Ignore
	public void testProcessor(){
		FutureStreamSubscriber<Long> sub = new FutureStreamSubscriber();
		System.out.println("End of chain sub " + System.identityHashCode(sub));
		FutureStreamProcessor proc =	new FutureStreamProcessor();
		proc.subscribe(sub);
		
		LazyFutureStream.of(1,2,3).subscribe(proc);
		
		
		
		assertThat(sub.getStream().toList(),equalTo(Arrays.asList(1,2,3)));
				
	}
	
	boolean errored =false;
	@Test
	public void test(){
		
		LazyFutureStream.<Long>generate(()-> 2l )
						.limit(1)
						.<Long>map(i-> {throw new RuntimeException();})
						.subscribe(new Subscriber<Long>() {

			@Override
			public void onSubscribe(Subscription s) {
				s.request(10);
				
			}

			@Override
			public void onNext(Long t) {
				System.out.println(t);
				
			}

			@Override
			public void onError(Throwable t) {
				errored=true;
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		});
		
		assertTrue(errored);
	}
	
	@Test @Ignore
	public void processorFromReactor() throws InterruptedException{
		
			final int elements = 1000;
			CountDownLatch latch = new CountDownLatch(elements + 1);

			Processor<Integer, Integer> processor = new FutureStreamProcessor();

			createHelperPublisher(elements).subscribe(processor);

			
			List<Integer> list = new ArrayList<>();

			processor.subscribe(new Subscriber<Integer>() {
				Subscription s;

				@Override
				public void onSubscribe(Subscription s) {
					this.s = s;
					s.request(1);
				}

				@Override
				public void onNext(Integer integer) {
					synchronized (list) {
						list.add(integer);
					}
					latch.countDown();
					if (latch.getCount() > 0) {
						s.request(1);
					}
				}

				@Override
				public void onError(Throwable t) {
					t.printStackTrace();
				}

				@Override
				public void onComplete() {
					System.out.println("completed!");
					latch.countDown();
				}
			});
			//stream.broadcastComplete();

			latch.await(8, TimeUnit.SECONDS);
			

			//System.out.println(counters);
			long count = latch.getCount();
			assertTrue(latch.getCount() == 0);
		}
	private LazyFutureStream createHelperPublisher(int elements) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < elements; i++) {
			list.add(i);
		}

		return LazyFutureStream.lazyFutureStreamFromIterable(list);
				
	
	}
}
