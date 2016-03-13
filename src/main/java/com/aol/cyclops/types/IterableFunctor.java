package com.aol.cyclops.types;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.Mutable;
import com.aol.cyclops.types.futurestream.Continuation;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.reactive.FlatMapConfig;
import com.aol.cyclops.types.stream.reactive.QueueBasedSubscriber;
import com.aol.cyclops.types.stream.reactive.QueueBasedSubscriber.Counter;

public interface IterableFunctor<T> extends Iterable<T>,Functor<T>, Foldable<T>, Traversable<T>,
											ConvertableSequence<T>{
  
    /**
      A potentially asynchronous merge operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the overloaded @see {@link IterableFunctor#mergeublisher(Collection, FlatMapConfig)} 
     * method to subscribe asynchronously also. A default limit of 10k active publishers is enforced, along with a default limit of 5k queued values before
     * backpressure is applied.
     * 
     * @param publishers
     * @return
     */
    default  ReactiveSeq<T> mergePublisher(Collection<? extends Publisher<T>> publishers){
        return mergePublisher(publishers,FlatMapConfig.defaultConfig());
    }
    /**
     * A potentially asynchronous merge operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the @see {@link FlatMapConfig} class to configure an executor for asynchronous subscription.
     * FlatMap config can also be used to place limits on the maximum active elements. A default limit of 10k active publishers is enforced.
     * 
     * 
     */
    default  ReactiveSeq<T> mergePublisher(Collection<? extends Publisher<T>> publishers, FlatMapConfig<T> config){
        Counter c = new Counter();
        c.active.set(publishers.size()+1);
        QueueBasedSubscriber<T> init = QueueBasedSubscriber.subscriber(config.factory,c);
       
        ReactiveSeq<T> stream = stream();
        
        Supplier<Continuation> sp = ()->{
               config.ex.peek(ex->{
                   
                   CompletableFuture.runAsync(()-> stream.subscribe(init),ex);
                   for(Publisher next : publishers){
                      
                       CompletableFuture.runAsync(()->  next.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(),c)),ex);
                      
                   }
                   
               })
                .orElseGet(()->{
                            
                            stream.subscribe(init);
                           for(Publisher next : publishers){
                               
                               next.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(),c));
                           }
                           return null;
                         });
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
        return flatMapPublisher(mapper,FlatMapConfig.defaultConfig());
    }
    /**
     * A potentially asynchronous flatMap operation where data from each publisher may arrive out of order (if publishers
     * are configured to publish asynchronously, users can use the @see {@link FlatMapConfig} class to configure an executor for asynchronous subscription.
     * FlatMap config can also be used to place limits on the maximum active elements. A default limit of 10k active publishers is enforced.
     * 
     * 
     */
    default <R> ReactiveSeq<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper, FlatMapConfig<R> config){
        Counter c = new Counter();
        QueueBasedSubscriber<R> init = QueueBasedSubscriber.subscriber(config.factory,c);
       
        ReactiveSeq<T> stream = stream();
        Supplier<Continuation> sp = ()->{
            
            stream.map(mapper).forEachEvent(p->{
                c.active.incrementAndGet();
                config.ex.peek(e->{  
                   
                            CompletableFuture.runAsync(()->{p.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(),c)); 
                                     },e);
                           
                          })
                         .orElseGet( ()->{
                            
                                 p.subscribe(QueueBasedSubscriber.subscriber(init.getQueue(),c)); 
                                
                                 return null;
                                 });
               
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
