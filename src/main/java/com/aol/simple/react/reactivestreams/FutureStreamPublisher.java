package com.aol.simple.react.reactivestreams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.StreamWrapper;

public interface FutureStreamPublisher<T> extends Publisher<T> {
	StreamWrapper getLastActive();
	void cancel();
	
	
	default void  subscribe(Subscriber<? super T> s){
	//	Function<CompletableFuture,U> safeJoin = (CompletableFuture cf)->(U) BlockingStreamHelper.getSafe(cf,getErrorHandler());
		
		try {
			Queue<T> queue = toQueue();
			Iterator<CompletableFuture<T>> it = queue.streamCompletableFutures().iterator();
			List<CompletableFuture> results = new ArrayList<>();
		
			Subscription sub = new Subscription(){
				volatile boolean complete =false;
				volatile boolean cancelled = false;
				
				@Override
				public void request(long n) {
					
					if(n<1){
						s.onError(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
					}
					
					
					if(!cancelled){
						for(int i=0;i<n;i++){
							if(it.hasNext()){
								//s.onNext(it.next());
								results.add(it.next().thenAccept( r-> s.onNext(r)).exceptionally(t->{ s.onError(t); return null;}));
								List<CompletableFuture> newResults = results.stream().filter(cf->cf.isDone()).collect(Collectors.toList());
								results.removeAll(newResults);
							}
							else{
								
								if(!complete && !cancelled){
									complete=true;
									s.onComplete();
								}
								break;
							}
							
							
						}
					}
				
					
				}

				@Override
				public void cancel() {
					//stopping[0] = true;
				//	results.stream().peek(cf ->cf.cancel(true));
					queue.closeAndClear();
					cancelled=true;
					
				}
				
			};
			s.onSubscribe(sub);
			
				
				
				
		
		} catch (SimpleReactProcessingException e) {
			
		}
	
	}
	Queue<T> toQueue();

	

}