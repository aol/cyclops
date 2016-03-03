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
import com.aol.cyclops.types.stream.reactive.QueueBasedSubscriber;

public class ErrorTest {
	@Test
	public void testSubscribe(){
		QueueBasedSubscriber<Integer> sub = new QueueBasedSubscriber();
		
		
		LazyFutureStream.of(1,2,3).subscribe(sub);
		
		
		sub.futureStream().forEach(System.out::println);
	}
	
	List<Integer> result;
	
	
	
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
	
	private LazyFutureStream createHelperPublisher(int elements) {
		List<Integer> list = new ArrayList<Integer>();
		for (int i = 0; i < elements; i++) {
			list.add(i);
		}

		return LazyFutureStream.lazyFutureStreamFromIterable(list);
				
	
	}
}
