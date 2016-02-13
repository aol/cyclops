package com.aol.cyclops.react.reactivestreams;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import com.aol.cyclops.react.async.Queue;
import com.aol.cyclops.react.exceptions.SimpleReactProcessingException;
import com.aol.cyclops.react.stream.LazyStreamWrapper;

/**
 * Reactive Streams publisher that uses a separate thread  (non-calling thread) to publish on
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface FutureStreamAsyncPublisher<T> extends Publisher<T> {
	LazyStreamWrapper<T> getLastActive();
	void cancel();
	void forwardErrors(Consumer<Throwable> c);
	FutureStreamAsyncPublisher<T> withPublisherExecutor(Executor ex);
	
	Executor getPublisherExecutor();
	default void  subscribeOn(Subscriber<? super T> s, Executor ex){
		this.withPublisherExecutor(ex).subscribeAsync(s);
	}
	
	default void subscribeAsync(Subscriber<? super T> s){
		FutureStreamAsyncPublisher.this.subscribe(s);
	}
	
	/* 
	 * @param s ReactiveStreams Subscriber
	 * @see org.reactivestreams.Publisher#subscribe(org.reactivestreams.Subscriber)
	 */
	default void  subscribe(Subscriber<? super T> s){
	
		try {
			
			forwardErrors(t->s.onError(t));
		
				
			Queue<T> queue = toQueue();
			Iterator<CompletableFuture<T>> it = queue.streamCompletableFutures().iterator();
			
			
			
			
			Subscription sub = new Subscription(){
				
				volatile boolean complete =false;
				
				volatile boolean cancelled = false;
				final ConcurrentLinkedQueue<Long> requests = new ConcurrentLinkedQueue<Long>();
				{
					
					CompletableFuture.runAsync( ()-> {
						processRequests(s, it); 
					}, getPublisherExecutor());
				}
				private void processRequests(Subscriber<? super T> s,
						Iterator<CompletableFuture<T>> it) {
					List<CompletableFuture> results = new ArrayList<>();
					while(!cancelled && !complete){
						
						activeLoop(s, it, results);
					
					}
				}
				private void activeLoop(Subscriber<? super T> s,
						Iterator<CompletableFuture<T>> it,
						List<CompletableFuture> results) {
					while(requests.size()>0){
						
						requestLoop(s, it, results);
						

					}
				}
				private void requestLoop(Subscriber<? super T> s,
						Iterator<CompletableFuture<T>> it,
						List<CompletableFuture> results) {
					long n2 = requests.peek();
					
					
					for(int i=0;i<n2;i++){
						try{
							if(it.hasNext()){
								handleNext(s, it, results);
							}
							else{
								
								handleComplete(s);
								break;
							}
						}catch(Throwable t){
							s.onError(t);
						}
						
						
					}
					
					requests.poll();
				}
				private void handleComplete(Subscriber<? super T> s) {
					if(!complete && !cancelled){
						complete=true;
						s.onComplete();
					}
				}
				private void handleNext(Subscriber<? super T> s,
						Iterator<CompletableFuture<T>> it,
						List<CompletableFuture> results) {
					results.add(it.next().thenAccept( r-> s.onNext(r)).exceptionally(t->{ s.onError(t); return null;}));
					List<CompletableFuture> newResults = results.stream().filter(cf->cf.isDone()).collect(Collectors.toList());
					results.removeAll(newResults);
				}
				
				

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