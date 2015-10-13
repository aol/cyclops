package com.aol.simple.react.reactivestreams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.LazyStreamWrapper;

/**
 * Reactive Streams publisher, that publishes on the calling thread
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface FutureStreamSynchronousPublisher<T> extends Publisher<T> {
	LazyStreamWrapper getLastActive();
	void cancel();
	void forwardErrors(Consumer<Throwable> c);
	
	
	default void subscribeSync(Subscriber<? super T> s){
		FutureStreamSynchronousPublisher.this.subscribe(s);
	}
	
	default void  subscribe(Subscriber<? super T> s){
	
		try {
			
			forwardErrors(t->s.onError(t));
		
				
			Queue<T> queue = toQueue();
			Iterator<CompletableFuture<T>> it = queue.streamCompletableFutures().iterator();
			
			
			
			
			Subscription sub = new Subscription(){
				
				volatile boolean complete =false;
				
				volatile boolean cancelled = false;
				final Stack<Long> requests = new Stack<Long>();
				
				
				
				

				private void handleNext(T data){
					if(!cancelled){ 
						s.onNext(data);
					}
						
				}
				@Override
				public void request(final long n) {
					
					if(n<1){
						s.onError(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
					}
					requests.add(n);
					if(requests.size()>1)
						return;
					List<CompletableFuture> results = new ArrayList<>();
					while(!cancelled  && requests.size()>0){
						long n2 = requests.peek();
						for(int i=0;i<n2;i++){
							
							if(it.hasNext()){
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
						requests.pop();
					}
				
					
				}

				@Override
				public void cancel() {
					
					cancelled=true;
					forwardErrors(t->{});
					queue.closeAndClear();
					
					
				}
				
			};
			s.onSubscribe(sub);
			
				
				
				
		
		} catch (SimpleReactProcessingException e) {
			
		}
	
	}
	Queue<T> toQueue();

	

}