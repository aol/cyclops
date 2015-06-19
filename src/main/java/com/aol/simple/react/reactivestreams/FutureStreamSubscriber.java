package com.aol.simple.react.reactivestreams;



import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Consumer;

import lombok.Getter;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.simple.react.async.Continueable;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.lazy.LazyReact;
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
				System.out.println("requesting! 1");
				s.request(1);
				System.out.println("waiting!");
				try{
					return (T)super.get();
				}
				finally{
					System.out.println("got!");
				}
				
			}
		};
	
		this.subscription= s;
		stream = stream();
		/**Continuation[] cont = new Continuation[1];
		cont[0]= new Continuation(() ->{  
			if(queue.isOpen())
				s.request(1);
			else{
				s.cancel();
				throw new ClosedQueueException();
			}
			return cont[0];  });
		queue.setContinuation(cont[0]);**/
		s.request(1);
		
	}

	@Override
	public void onNext(T t) {
		System.out.println("On next! " + t);
		Objects.requireNonNull(t);
		queue.add(t);
		System.out.println("Added " + queue.size());
		
	}

	@Override
	public void onError(Throwable t) {
		System.out.println("On error!");
		Objects.requireNonNull(t);
		((Consumer)stream.getErrorHandler().orElse((Consumer)h->{})).accept(t);
		
	}

	@Override
	public void onComplete() {
		System.out.println("On complete!");
		if(queue!=null){
			queue.setContinuation(new Continuation( () -> {
						throw new ClosedQueueException();
			}));
			queue.close();
		}
		
		
	}

}
