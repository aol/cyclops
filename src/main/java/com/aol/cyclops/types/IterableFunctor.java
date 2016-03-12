package com.aol.cyclops.types;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jooq.lambda.Collectable;
import org.reactivestreams.Publisher;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.futurestream.Continuation;
import com.aol.cyclops.types.stream.ConvertableSequence;
import com.aol.cyclops.types.stream.reactive.FlatMapConfig;
import com.aol.cyclops.types.stream.reactive.QueueBasedSubscriber;

public interface IterableFunctor<T> extends Iterable<T>,Functor<T>, Foldable<T>, Traversable<T>,
											ConvertableSequence<T>{
  
    default <R> ReactiveSeq<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper){
        return flatMapPublisher(mapper,FlatMapConfig.defaultConfig());
    }
    default <R> ReactiveSeq<R> flatMapPublisher(Function<? super T, ? extends Publisher<? extends R>> mapper, FlatMapConfig<R> config){
        QueueBasedSubscriber<R> sub=  QueueBasedSubscriber.subscriber(config.factory);
        ReactiveSeq<T> stream = stream();
        Supplier<Continuation> sp = ()->{
            stream.map(mapper).forEachEvent(p->{
                config.ex.peek(e-> CompletableFuture.runAsync(()->p.subscribe(sub),e))
                         .orElseGet( ()->{p.subscribe(sub); return null;});
                } ,i->{} ,()->{ sub.close();});
            return Continuation.empty();
        };
        Continuation continuation = new Continuation(sp);
        sub.addContinuation(continuation);
        return ReactiveSeq.fromStream(sub.jdkStream());
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
