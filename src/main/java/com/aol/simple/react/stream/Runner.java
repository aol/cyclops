package com.aol.simple.react.stream;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.traits.Continuation;

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
						
						
					}catch(Throwable e){
						
					}
					return finishNoCollect;
							
				});
				
			
			return cont[0];
		
		

	}
	
	private <T> void handleFilter(Continuation[] cont, FastFuture<T> f){
		f.essential( event -> {
			
			if (event.exception !=null && (event.exception.getCause() instanceof FilteredExecutionPathException)) {
				
				 cont[0].proceed();
				
			}
			
		});
	}
	
	
}
