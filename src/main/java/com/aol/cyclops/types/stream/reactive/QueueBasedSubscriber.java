package com.aol.cyclops.types.stream.reactive;



import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.control.FluentFunctions;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.types.futurestream.Continuation;
import com.aol.cyclops.types.futurestream.LazyFutureStream;

import lombok.Getter;
import lombok.Setter;

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
public class QueueBasedSubscriber<T> implements Subscriber<T> {
	
    public static <T> QueueBasedSubscriber<T> subscriber(){
        return new QueueBasedSubscriber<>();
    }
    public static <T> QueueBasedSubscriber<T> subscriber(QueueFactory<T> factory){
        
        return new QueueBasedSubscriber<>();
    }
	
    private Stream<T> genJdkStream(){
        Continueable subscription =  new com.aol.cyclops.react.async.subscription.Subscription();
        return queue.stream(subscription);
    }
	private LazyFutureStream<T> genStream(){
		Continueable subscription =  new com.aol.cyclops.react.async.subscription.Subscription();
		return LazyFutureStream.of()
					.withSubscription(subscription)
					.fromStream(queue.stream(subscription));
	}
	private final QueueFactory<T> factory;
	protected volatile Queue<T> queue;
	@Getter
	volatile Subscription subscription;
	
	private volatile LazyFutureStream<T> stream;
	private volatile Supplier<LazyFutureStream<T>> futureStream;
	private volatile Supplier<Stream<T>> jdkStream;
	private volatile Supplier<ReactiveSeq<T>> reactiveSeq;
	@Setter
	private volatile Consumer<Throwable> errorHandler;
	
	public QueueBasedSubscriber(){
	    this(null);
	}
	private QueueBasedSubscriber(QueueFactory<T> factory){
	    this.factory=factory;
	}
	
	public LazyFutureStream<T> futureStream(){
	    return stream = futureStream.get();
	}
	public Stream<T> jdkStream(){
        return jdkStream.get();
    }
	public ReactiveSeq<T> reactiveSeq(){
        return reactiveSeq.get();
    }
	
	@Override
	public void onSubscribe(final Subscription s) {
		Objects.requireNonNull(s);
	
		if(this.subscription!=null){
			s.cancel();
			return;
		}
		
		if(factory!=null) {
    		queue = new Queue<T>(factory){
    			public T get(){
    				s.request(1);
    				return (T)super.get();	
    			}
    		};
		}else{
		    queue = new Queue<T>(){
                public T get(){
                    s.request(1);
                    return (T)super.get();  
                }
            };
		}
	
		this.subscription= s;
		
		s.request(1);
		futureStream= FluentFunctions.of(this::genStream)
		                             .memoize();
		jdkStream= FluentFunctions.of(this::genJdkStream)
                                  .memoize();
		reactiveSeq = FluentFunctions.of(()->ReactiveSeq.fromStream(jdkStream.get()))
		                            .memoize();
		
	}

	@Override
	public void onNext(T t) {
		
		Objects.requireNonNull(t);
		queue.add(t);
		
		
	}

	@Override
	public void onError(Throwable t) {
		
		Objects.requireNonNull(t);
		if(stream!=null)
		    ((Consumer)stream.getErrorHandler().orElse((Consumer)h->{})).accept(t);
		if(errorHandler!=null)
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
