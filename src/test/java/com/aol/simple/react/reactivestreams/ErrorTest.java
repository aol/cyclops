package com.aol.simple.react.reactivestreams;

import com.aol.simple.react.stream.traits.LazyFutureStream;

import org.junit.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
public class ErrorTest {

	@Test
	public void test(){
		LazyFutureStream.<Long>generate(()-> 2l ).<Long>map(i-> {throw new RuntimeException();}).limit(1).subscribe(new Subscriber<Long>() {

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
				t.printStackTrace();
				
			}

			@Override
			public void onComplete() {
				// TODO Auto-generated method stub
				
			}
		});;
	}
}
