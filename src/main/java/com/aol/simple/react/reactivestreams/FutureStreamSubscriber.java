package com.aol.simple.react.reactivestreams;



import java.util.Iterator;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import lombok.Getter;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.traits.Continuation;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public class FutureStreamSubscriber<T> implements Subscriber<T> {
	
	public LazyFutureStream stream(){
		return LazyFutureStream.of(queue.stream());
	}
	private Queue queue;
	@Getter
	volatile Subscription s;
	private LazyFutureStream stream;
	
	@Override
	public void onSubscribe(final Subscription s) {
		Objects.requireNonNull(s);
	
		if(this.s!=null){
			s.cancel();
			return;
		}
		queue = new Queue();
	
		this.s= s;
		stream = stream();
		Continuation[] cont = new Continuation[1];
		cont[0]= new Continuation(() ->{  s.request(1); return cont[0];  });
		queue.setContinuation(cont[0]);
		s.request(1);
		
	}

	@Override
	public void onNext(T t) {
		Objects.requireNonNull(t);
		queue.offer(t);
		
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
