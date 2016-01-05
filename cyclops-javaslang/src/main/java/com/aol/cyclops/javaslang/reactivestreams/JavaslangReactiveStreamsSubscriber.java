package com.aol.cyclops.javaslang.reactivestreams;

import java.util.Objects;
import java.util.function.Consumer;

import javaslang.collection.Stream;
import lombok.Getter;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.javaslang.FromJDK;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.reactivestreams.JDKReactiveStreamsSubscriber;
import com.aol.simple.react.stream.traits.Continuation;

public class JavaslangReactiveStreamsSubscriber<T> implements Subscriber<T> {

	
	protected java.util.stream.Stream<T> stream(){
		Continueable subscription =  new com.aol.simple.react.async.subscription.Subscription();
		return queue.stream(subscription);
	}
	protected volatile Queue<T> queue;
	@Getter
	volatile Subscription subscription;
	private volatile java.util.stream.Stream jdkStream;
	
	public Stream<T> getStream(){
		return FromJDK.stream(jdkStream);
	}
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
		jdkStream = stream();
		
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