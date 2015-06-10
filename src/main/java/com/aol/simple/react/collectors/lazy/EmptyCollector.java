package com.aol.simple.react.collectors.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.stream.traits.ConfigurableStream;
/**
 * A collector that periodically joins active completablefutures
 * but does not store the results
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@Wither
@AllArgsConstructor
public class EmptyCollector<T> implements LazyResultConsumer<T> {
	

	private final List<CompletableFuture<T>> active = new ArrayList<>();
	@Getter
	private final MaxActive maxActive;
	
	public EmptyCollector(){
		maxActive = MaxActive.defaultValue.factory.getInstance();
	}
	
	/* 
	 *	@param t Result type
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(CompletableFuture<T> t) {
		active.add(t);
		
		if(active.size()>maxActive.getMaxActive()){
			
			while(active.size()>maxActive.getReduceTo()){
				
				
				
				List<CompletableFuture> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
				active.removeAll(toRemove);
				if(active.size()>maxActive.getReduceTo()){
					CompletableFuture promise=  new CompletableFuture();
					CompletableFuture.anyOf(active.toArray(new CompletableFuture[0]))
									.thenAccept(cf -> promise.complete(true));
					
					promise.join();
				}
				
					
			}
		}
		
		
		
	}

	
	@Override
	public LazyResultConsumer<T> withResults(Collection<CompletableFuture<T>> t) {
		
		return this.withMaxActive(maxActive);
	}

	/* 
	 *	@return empty list
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getResults()
	 */
	@Override
	public Collection<CompletableFuture<T>> getResults() {
		active.stream().forEach(cf -> cf.join());
		active.clear();
		return new ArrayList<>();
	}
	/* 
	 *	@return empty list
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getAllResults()
	 */
	public Collection<CompletableFuture<T>> getAllResults() {
		return getResults();
	}

	/* 
	 *	@return null
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getBlocking()
	 */
	@Override
	public ConfigurableStream<T> getBlocking() {
	
		return null;
	}
	
}