package com.aol.simple.react.reactivestreams;

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

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.StreamWrapper;

public interface FutureStreamPublisher<T> extends Publisher<T> {
	StreamWrapper getLastActive();
	void cancel();
	void forwardErrors(Consumer<Throwable> c);
	org.reactivestreams.Subscription getReactiveStreamsSubscription();
	FutureStreamPublisher<T> withReactiveStreamsSubscription(org.reactivestreams.Subscription subscription);
	Executor getPublisherExecutor();
	default void  subscribe(Subscriber<? super T> s){
	
		try {
			
			forwardErrors(t->s.onError(t));
		
				
			Queue<T> queue = toQueue();
			Iterator<CompletableFuture<T>> it = queue.streamCompletableFutures().iterator();
			
			
			
			
			Subscription sub = new Subscription(){
				{
				System.out.println(" New subscription " +  System.identityHashCode(this));
				}
				volatile boolean complete =false;
				
				volatile boolean cancelled = false;
				ConcurrentLinkedQueue<Long> requests = new ConcurrentLinkedQueue<Long>();
				{
					CompletableFuture.runAsync( ()-> {
						List<CompletableFuture> results = new ArrayList<>();
						while(!cancelled && !complete){
							
						while(requests.size()>0){
							System.out.println("Looping Requests " + requests.size() + " : " + System.identityHashCode(requests) + " : cf " + System.identityHashCode(this));

							long n2 = requests.peek();
							System.out.println("processing " + n2 + " : " + System.identityHashCode(requests) + " : cf " + System.identityHashCode(this));
							handleDownstream(n2);
							for(int i=0;i<n2;i++){
								System.out.println("Calling it.next()" +  " : " + System.identityHashCode(requests) + " : cf " + System.identityHashCode(this));
								if(it.hasNext()){
									System.out.println("HAS NEXT " + n2 + " : " + System.identityHashCode(requests) + " : cf " + System.identityHashCode(this));
									results.add(it.next().thenAccept( r-> s.onNext(r)).exceptionally(t->{ s.onError(t); return null;}));
									List<CompletableFuture> newResults = results.stream().filter(cf->cf.isDone()).collect(Collectors.toList());
									results.removeAll(newResults);
									System.out.println("HAS NEXT-DONE " + n2 + " : " + System.identityHashCode(requests) + " : cf " + System.identityHashCode(this));
									
								}
								else{
									
									if(!complete && !cancelled){
										complete=true;
										s.onComplete();
									}
									break;
								}
								
								
							}
							System.out.println("POLLING Looping Requests  " + requests.size() + " : " + System.identityHashCode(requests) + " : cf " + System.identityHashCode(this));
							requests.poll();
							System.out.println("FINISHED Looping Requests CYCLE " + requests.size() + " : " + System.identityHashCode(requests) + " : cf " + System.identityHashCode(this));

									}
						
					} 
						System.out.println("not processing anymore!");
					
					}, getPublisherExecutor());
				}
				private void handleDownstream(Long numElements){
					if(!cancelled){ 
						if(getReactiveStreamsSubscription()!=null){
							System.out.println("Forwarding demand " + numElements);
							getReactiveStreamsSubscription().request(numElements);
							
						}
					}
				}
				
				

				private void handleNext(T data){
					if(!cancelled){ 
						s.onNext(data);
					}
						
				}
				@Override
				public void request(final long n) {
					System.out.println("Recieved " +  n  +" : sub : "+ System.identityHashCode(this));
					if(n<1){
						s.onError(new IllegalArgumentException("3.9 While the Subscription is not cancelled, Subscription.request(long n) MUST throw a java.lang.IllegalArgumentException if the argument is <= 0."));
					}
					
					requests.add(n);
				//	System.out.println("Requests " + requests.size() + " : " + System.identityHashCode(requests) + " : sub : "+ System.identityHashCode(this));
				/**	SimpleReact reactor = SequentialElasticPools.simpleReact.nextReactor();
					reactor.react( ()->n)//.peek(this::handleDownstream)
						//new SimpleReact(Executors.newCachedThreadPool()).react( ()->n)
										.peek( n2 -> {
											//		handleDownstream(n2);
													List<CompletableFuture> results = new ArrayList<>();
													for(int i=0;i<n2;i++){
														if(cancelled)
															break;
														if(it.hasNext()){
															results.add(it.next().thenAccept(this::handleNext).exceptionally(t->{ s.onError(t); return null;}));
															List<CompletableFuture> newResults = results.stream().filter(cf->cf.isDone()).collect(Collectors.toList());
															results.removeAll(newResults);
															
														}
														else{
															
															if(!complete && !cancelled){
																complete=true;
																results.stream().forEach(cf -> cf.join());
																s.onComplete();
															}
															break;
														}
														
														
													}
													
													
											}).allOf( r -> { SequentialElasticPools.simpleReact.populate(reactor); return null;});
					
					
					
					**/
					
					/**
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
					}**/
				
					
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