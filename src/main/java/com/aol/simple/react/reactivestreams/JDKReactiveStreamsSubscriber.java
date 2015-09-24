package com.aol.simple.react.reactivestreams;



import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.Getter;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.stream.traits.Continuation;

/**
 * 
 * ReactiveStreams subscriber for standard Java 8 Stream implementations including
 * 
 * 
 *
 * @author johnmcclean
 *
 * @param <T>
 */
public class JDKReactiveStreamsSubscriber<T> implements Subscriber<T> {
	
	
	protected Stream<T> stream(){
		Continueable subscription =  new com.aol.simple.react.async.subscription.Subscription();
		return queue.stream(subscription);
	}
	protected volatile Queue<T> queue;
	@Getter
	volatile Subscription subscription;
	@Getter
	protected volatile Stream<T> stream;
	volatile Consumer errorHandler = e -> { };
	
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
		errorHandler.accept(t);
		
	}

	@Override
	public void onComplete() {
		
		if(queue!=null){
			queue.addContinuation(new Continuation( () -> {
						throw new ClosedQueueException();
			}));
			queue.close();
		}
		
		
	}

}
