package com.aol.simple.react.reactivestreams;



import java.util.Objects;
import java.util.function.Consumer;

import lombok.Getter;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.simple.react.async.Continueable;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.stream.traits.Continuation;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class FutureStreamSubscriber<T> implements Subscriber<T> {
	
	
	protected LazyFutureStream stream(){
		Continueable subscription =  new com.aol.simple.react.async.Subscription();
		return LazyFutureStream.of()
					.withSubscription(subscription)
					.fromStream(queue.stream(subscription));
	}
	protected Queue queue;
	@Getter
	volatile Subscription subscription;
	@Getter
	protected LazyFutureStream stream;
	
	@Override
	public void onSubscribe(final Subscription s) {
		Objects.requireNonNull(s);
	
		if(this.subscription!=null){
			s.cancel();
			return;
		}
		
		queue = new Queue(){
			public T get(){
				s.request(1);
				
					return (T)super.get();	
			}
		};
	
		this.subscription= s;
		stream = stream();
		
		s.request(1);
		
	}

	@Override
	public void onNext(T t) {
		
		Objects.requireNonNull(t);
		queue.add(t);
		
		
	}

	@Override
	public void onError(Throwable t) {
		
		Objects.requireNonNull(t);
		((Consumer)stream.getErrorHandler().orElse((Consumer)h->{})).accept(t);
		
	}

	@Override
	public void onComplete() {
		
		if(queue!=null){
			queue.setContinuation(new Continuation( () -> {
						throw new ClosedQueueException();
			}));
			queue.close();
		}
		
		
	}

}
