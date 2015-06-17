package com.aol.simple.react.stream;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import lombok.AllArgsConstructor;

import com.aol.simple.react.async.Queue.ClosedQueueException;
import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.exceptions.ExceptionSoftener;
import com.aol.simple.react.exceptions.FilteredExecutionPathException;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.traits.Continuation;

@AllArgsConstructor
public class Runner {

	private final Runnable runnable;
	
	public boolean  run(StreamWrapper lastActive,EmptyCollector collector) {

		

		try {
			lastActive.stream().forEach(n -> {

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
	public Continuation  runContinuations(StreamWrapper lastActive,EmptyCollector collector) {

		Iterator<CompletableFuture> it = lastActive.stream().iterator();
		
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
							
							CompletableFuture f = it.next();
							
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
	
	
}
