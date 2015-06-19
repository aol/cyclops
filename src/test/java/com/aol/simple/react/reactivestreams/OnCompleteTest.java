package com.aol.simple.react.reactivestreams;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

public class OnCompleteTest {
	List<Integer> result;
	boolean found;
	@Test
	public void testProcessorPublish() throws InterruptedException{
		found = true;
		result = new ArrayList<>();
		FutureStreamProcessor<Integer,Integer> pub = new FutureStreamProcessor();
		
		Subscriber subscriber =new Subscriber<Integer>() {

			@Override
			public void onSubscribe(Subscription s) {
				//s.request(50000);
				
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
				found = true;
			}
		};
		pub.subscribe(subscriber);
		pub.onSubscribe(new Subscription() {
			
			@Override
			public void request(long n) {
				/**for(int i=1;i<n;i++)
					subscriber.onNext(i);
				subscriber.onComplete();
				**/
			}
			
			@Override
			public void cancel() {
				// TODO Auto-generated method stub
				
			}
		});
		pub.onComplete();
		Thread.sleep(100);
		
		System.out.println("result " + result);
		assertThat(found,equalTo(true));
		
	}
}
