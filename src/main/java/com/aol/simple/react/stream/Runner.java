package com.aol.simple.react.stream;

import lombok.AllArgsConstructor;

import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;

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
	
}
