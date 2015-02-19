package com.aol.simple.react.collectors.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.LockSupport;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.config.MaxActive;

/**
 * This class allows a Batch of completable futures to be processed before collecting their results, to increase
 * parallelism.
 * 
 * @author johnmcclean
 *
 * @param <T> Result type
 */
@Wither
@AllArgsConstructor
@Builder
public class BatchingCollector<T> implements LazyResultConsumer<T>{

	
	private final Collection<T> results;
	private final List<CompletableFuture<T>> active = new ArrayList<>();
	@Getter
	private final MaxActive maxActive;
	
	/**
	 * @param maxActive Controls batch size
	 */
	public BatchingCollector(MaxActive maxActive){
		this.maxActive = maxActive;
		this.results =null;
	}
	/**
	 * Batching Collector with default Max Active settings
	 */
	public BatchingCollector(){
		this.maxActive = MaxActive.defaultValue.factory.getInstance();
		this.results =null;
	}
	
	/* (non-Javadoc)
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(CompletableFuture<T> t) {
		active.add(t);
		
		if(active.size()>maxActive.getMaxActive()){
			
			while(active.size()>maxActive.getReduceTo()){
				LockSupport.parkNanos(0l);
				List<CompletableFuture> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
				active.removeAll(toRemove);
				results.addAll((Collection<? extends T>) toRemove.stream().map(cf -> cf.join()).collect(Collectors.toList()));
				
			}
		}
		
		
		
	}
	/* (non-Javadoc)
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getResults()
	 */
	public Collection<T> getResults(){
		moveBatchToResult();
		return results;
	}

	private void moveBatchToResult() {
		results.addAll(active.stream().map(cf -> cf.join()).collect(Collectors.toList()));
	}
	
	
}
