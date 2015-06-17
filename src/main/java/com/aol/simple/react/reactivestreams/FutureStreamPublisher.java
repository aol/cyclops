package com.aol.simple.react.reactivestreams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.traits.BlockingStreamHelper;
import com.aol.simple.react.stream.traits.LazyFutureStream;

public interface FutureStreamPublisher<T> extends Publisher<T> {
	StreamWrapper getLastActive();
	
	default void  subscribe(Subscriber<? super T> s){
	//	Function<CompletableFuture,U> safeJoin = (CompletableFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		
		try {
			Iterator<CompletableFuture<T>> it = (Iterator)getLastActive().stream().iterator();
			
			Subscription sub = new Subscription(){

				@Override
				public void request(long n) {
					
					if(n<1){
						s.onError(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
					}
					List<CompletableFuture<T>> results = new ArrayList<>();
					for(int i=0;i<n;i++){
						if(it.hasNext())
							results.add(it.next());
					}
					results.stream().map(cf -> cf.thenAccept( r-> s.onNext(r)).exceptionally(t->{ s.onError(t); return null;}));
					
				}

				@Override
				public void cancel() {
					this.cancel();
					
				}
				
			};
			s.onSubscribe(sub);
			
				
				
				
		
		} catch (SimpleReactProcessingException e) {
			
		}
		s.onComplete();
	}

}