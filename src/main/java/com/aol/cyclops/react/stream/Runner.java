package com.aol.cyclops.react.stream;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;

import com.aol.cyclops.react.async.Queue.ClosedQueueException;
import com.aol.cyclops.react.async.future.FastFuture;
import com.aol.cyclops.react.collectors.lazy.EmptyCollector;
import com.aol.cyclops.react.exceptions.FilteredExecutionPathException;
import com.aol.cyclops.react.exceptions.SimpleReactProcessingException;
import com.aol.cyclops.react.stream.traits.Continuation;


@AllArgsConstructor
public class Runner<U> {

	private final Runnable runnable;
	
	public boolean  run(LazyStreamWrapper<U> lastActive,EmptyCollector<U> collector) {

		

		try {
			lastActive.injectFutures().forEach(n -> {

				collector.accept(n);
			});
			collector.getResults();
		} catch (SimpleReactProcessingException e) {
		
		}catch(java.util.concurrent.CompletionException e){
			
		}catch(Throwable e){
			
		}
		
		runnable.run();
		return true;

	}
	public Continuation  runContinuations(LazyStreamWrapper lastActive,EmptyCollector collector) {

		
		Iterator<FastFuture> it = lastActive.injectFutures().iterator();
		
			Continuation[] cont  = new Continuation[1];
				
				
				Continuation finish = new Continuation( () -> {
					
					collector.getResults();
					runnable.run();
					throw new ClosedQueueException();
				
				});
				Continuation finishNoCollect = new Continuation( () -> {
					runnable.run();
					
					throw new ClosedQueueException();
					
				});
			
				cont[0] =  new Continuation( () -> {	
					try {
						
							
							
							if(it.hasNext()){
								
								
								FastFuture f = it.next();
								
								handleFilter(cont,f);//if completableFuture has been filtered out, we need to move to the next one instead
									
								collector.accept(f);
							}
						
						if(it.hasNext())
							return cont[0];
						else {
							return finish.proceed();
						}
					} catch (SimpleReactProcessingException e) {
						
						
					}catch(java.util.concurrent.CompletionException e){
						
						
					}
					catch(Throwable e){
						
						collector.getSafeJoin().apply(FastFuture.failedFuture(e));
					}
					return finishNoCollect;
							
				});
				
			
			return cont[0];
		
		

	}
	
	private <T> void handleFilter(Continuation[] cont, FastFuture<T> f){
		AtomicInteger called=  new AtomicInteger(0);
		f.essential( event -> {
			
			if (event.exception !=null && (event.exception.getCause() instanceof FilteredExecutionPathException)) {
				if(called.compareAndSet(0, 1))
					cont[0].proceed();
				
			}
			
		});
	}
	
	
}
