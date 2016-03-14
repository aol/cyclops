package com.aol.cyclops.types;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.types.futurestream.Continuation;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.reactive.QueueBasedSubscriber;
import com.aol.cyclops.types.stream.reactive.QueueBasedSubscriber.Counter;

public interface IterableFunctor<T> extends Iterable<T>,Functor<T>, Foldable<T>, Traversable<T>,
											ConvertableSequence<T>{
  
    /**
      A potentially asynchronous merge operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the overloaded @see {@link IterableFunctor#mergeublisher(Collection, FlatMapConfig)} 
     * method to subscribe asynchronously also. Max concurrency is determined by the publishers collection size, along with a default limit of 5k queued values before
     * backpressure is applied.
     * 
     * @param publishers
     * @return
     */
    default  ReactiveSeq<T> mergePublisher(Collection<? extends Publisher<T>> publishers){
        return mergePublisher(publishers,QueueFactories.boundedQueue(5_000));
    }
    
    /**
     * A potentially asynchronous merge operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the @see {@link FlatMapConfig} class to configure an executor for asynchronous subscription.
     * The QueueFactory parameter can be used to control the maximum queued elements @see {@link QueueFactories}
     * 
     * 
     */
    default  ReactiveSeq<T> mergePublisher(Collection<? extends Publisher<T>> publishers, QueueFactory<T> factory){
        Counter c = new Counter();
        c.active.set(publishers.size()+1);
        QueueBasedSubscriber<T> init = QueueBasedSubscriber.subscriber(factory,c,publishers.size());
       
       
        Executor e = Executors.newFixedThreadPool(1);
        Supplier<Continuation> sp = ()->{
              subscribe(init);
              for(Publisher next : publishers){
                     next.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(),c,publishers.size()));
               }
                   
               init.close();
                
            return Continuation.empty();
        };
        Continuation continuation = new Continuation(sp);
        init.addContinuation(continuation);
        return ReactiveSeq.fromStream(init.jdkStream());
    }
    /**
     * A potentially asynchronous flatMap operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the overloaded @see {@link IterableFunctor#flatMapPublisher(Function, FlatMapConfig)} 
     * method to subscribe asynchronously also. A default limit of 10k active publishers is enforced, along with a default limit of 5k queued values before
     * backpressure is applied.
     * 
     * @param mapper
     * @return
     */
    default <R> ReactiveSeq<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper){
        return flatMapPublisher(mapper,10_000);
    }
    /**
     * A potentially asynchronous flatMap operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the overloaded @see {@link IterableFunctor#flatMapPublisher(Function, FlatMapConfig)} 
     * method to subscribe asynchronously also. Active publishers are limited by the maxConcurrency parameter, along with a default limit of 5k queued values before
     * backpressure is applied.
     * 
     * @param mapper
     * @return
     */
    default <R> ReactiveSeq<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper, int maxConcurrency){
        return flatMapPublisher(mapper,maxConcurrency, QueueFactories.boundedQueue(5_000));
    }
    /**
     * A potentially asynchronous flatMap operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the @see {@link FlatMapConfig} class to configure an executor for asynchronous subscription.
     * Active publishers are limited by the maxConcurrency parameter. The QueueFactory parameter can be used to control the maximum queued elements @see {@link QueueFactories}
     * 
     * 
     */
    default <R> ReactiveSeq<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper, int maxConcurrency,QueueFactory<R> factory){
        Counter c = new Counter();
        QueueBasedSubscriber<R> init = QueueBasedSubscriber.subscriber(factory,c,maxConcurrency);
       
        ReactiveSeq<T> stream = stream();
        Supplier<Continuation> sp = ()->{
            
            stream.map(mapper).forEachEvent(p->{
                c.active.incrementAndGet();
                p.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(),c,maxConcurrency)); 
                
                } ,
                i->{} ,
                ()->{ init.close(); });
            
            return Continuation.empty();
        };
        Continuation continuation = new Continuation(sp);
        init.addContinuation(continuation);
        return ReactiveSeq.fromStream(init.jdkStream());
    }
	<U> IterableFunctor<U> unitIterator(Iterator<U> U);
	<R> IterableFunctor<R>  map(Function<? super T,? extends R> fn);
	
	default  ReactiveSeq<T> stream(){
		return ReactiveSeq.fromIterable(this);
	}
	default  Collectable<T> collectable(){
		return stream().collectable();
	}
	
	
	
	
   
}
