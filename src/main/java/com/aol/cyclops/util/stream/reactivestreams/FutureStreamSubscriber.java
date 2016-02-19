package com.aol.cyclops.util.stream.reactivestreams;



import java.util.Objects;
import java.util.function.Consumer;

import lombok.Getter;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.types.futurestream.Continuation;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

/**
 * Create a LazyFutureStream Subscriber
 * 
 * to use 
 * 
 * <pre>
 * {@code 
 * FutureStreamSubscriber<Long> sub = new FutureStreamSubscriber<>();
 * reactivePublisher.subscribe(sub);
 * LazyFutureStream<Long> stream = sub.getStream();
 * 
 * }</pre>
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public class FutureStreamSubscriber<T> implements Subscriber<T> {
	
	
	protected LazyFutureStream stream(){
		Continueable subscription =  new com.aol.cyclops.react.async.subscription.Subscription();
		return LazyFutureStream.of()
					.withSubscription(subscription)
					.fromStream(queue.stream(subscription));
	}
	protected volatile Queue queue;
	@Getter
	volatile Subscription subscription;
	@Getter
	protected volatile LazyFutureStream stream;
	
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
			queue.addContinuation(new Continuation( () -> {
						throw new ClosedQueueException();
			}));
			queue.close();
		}
		
		
	}

}
