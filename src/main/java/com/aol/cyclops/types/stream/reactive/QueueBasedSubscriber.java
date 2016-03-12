package com.aol.cyclops.types.stream.reactive;



import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.control.Eval;
import com.aol.cyclops.control.FluentFunctions;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.Queue.ClosedQueueException;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.data.collections.extensions.standard.QueueX;
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
        
        return new QueueBasedSubscriber<>(factory);
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
	volatile QueueX<Subscription> subscription = QueueX.fromIterable(Collectors.toCollection(()->new ConcurrentLinkedQueue<Subscription>()), Arrays.<Subscription>asList());
	
	private volatile LazyFutureStream<T> stream;
	private volatile Supplier<LazyFutureStream<T>> futureStream = Eval.later(this::genStream);
	private volatile Supplier<Stream<T>> jdkStream = Eval.later(this::genJdkStream);
	private volatile Supplier<ReactiveSeq<T>> reactiveSeq = Eval.later(()->ReactiveSeq.fromStream(jdkStream.get()));
	@Setter
	private volatile Consumer<Throwable> errorHandler;
	
	private final AtomicInteger completed = new AtomicInteger(0);
	
	public QueueBasedSubscriber(){
	    factory=null;
	    queue = new Queue<T>(){
            public T get(){
                subscription.forEach(s->s.request(1));
               
                return (T)super.get();  
            }
        };
	}
	private QueueBasedSubscriber(QueueFactory<T> factory){
	    
	    this.factory=factory;
	  
            queue = new Queue<T>(factory){
                public T get(){
                    subscription.forEach(s->s.request(1));
                   
                    return (T)super.get();  
                }
            };
        
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
	
		
		if(this.subscription.contains(s)){
		    subscription.forEach(Subscription::cancel);
			
			return;
		}
		
		
	
		this.subscription.plus(s);
		
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
		System.out.println("next! " + t);
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
		
		if(queue!=null && closeable && completed.incrementAndGet()== subscription.size()){
		    
			queue.addContinuation(new Continuation( () -> {
						throw new ClosedQueueException();
			}));
			queue.close();
		}
		
		
	}
	volatile boolean closeable = false;
	public void close(){
	    closeable = true;
	}
	public void addContinuation(Continuation c){
	    queue.addContinuation(c);
	}

}
