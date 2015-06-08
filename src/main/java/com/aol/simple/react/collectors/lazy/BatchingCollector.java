package com.aol.simple.react.collectors.lazy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.stream.MissingValue;
import com.aol.simple.react.stream.traits.BlockingStream;

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

	@Getter
	private final Collection<CompletableFuture<T>> results;
	private final List<CompletableFuture<T>> active = new ArrayList<>();
	@Getter
	private final MaxActive maxActive;
	@Getter
	private final BlockingStream<T> blocking;
	
	/**
	 * @param maxActive Controls batch size
	 */
	public BatchingCollector(MaxActive maxActive,BlockingStream<T> blocking){
		this.maxActive = maxActive;
		this.results =null;
		this.blocking = blocking;
	}
	/**
	 * Batching Collector with default Max Active settings
	 */
	public BatchingCollector(BlockingStream<T> blocking){
		this.maxActive = MaxActive.defaultValue.factory.getInstance();
		this.results =null;
		this.blocking = blocking;
	}
	
	/* (non-Javadoc)
	 * @see java.util.function.Consumer#accept(java.lang.Object)
	 */
	@Override
	public void accept(CompletableFuture<T> t) {
		active.add(t);
		
		if(active.size()>maxActive.getMaxActive()){
			
			while(active.size()>maxActive.getReduceTo()){
				
				
				List<CompletableFuture<T>> toRemove = active.stream().filter(cf -> cf.isDone()).collect(Collectors.toList());
				active.removeAll(toRemove);
				results.addAll(toRemove);
				if(active.size()>maxActive.getReduceTo()){
					CompletableFuture promise=  new CompletableFuture();
					CompletableFuture.anyOf(active.toArray(new CompletableFuture[0]))
									.thenAccept(cf -> promise.complete(true));
					
					promise.join();
				}
				
			}
		}
		
		
		
	}
	
	/* (non-Javadoc)
	 * @see com.aol.simple.react.collectors.lazy.LazyResultConsumer#getResults()
	 */
	public Collection<CompletableFuture<T>> getResults(){
		results.addAll(active);
		return results;
	}

	 
	public void forEach(Consumer<? super T> c, Function<CompletableFuture,T> safeJoin){
		if(this.blocking.isParallel()  && results.size()>maxActive.getParallelReduceBatchSize()){
			forEachResults(getResults(),c, safeJoin);
		}else if(!this.blocking.isParallel()  && results.size()>maxActive.getMaxActive()){
			forEachResults(getResults(),c, safeJoin);
		}
	}
	public void forEachResults( Collection<CompletableFuture<T>> results,Consumer<? super T> c,
			Function<CompletableFuture, T> safeJoin) {
		Stream<CompletableFuture<T>> stream = getResults().stream();
		Stream<CompletableFuture<T>> streamToUse = this.blocking.isParallel() ? stream.parallel() : stream;
		streamToUse.map(safeJoin).filter(v -> v != MissingValue.MISSING_VALUE).forEach(c);
		getResults().clear();
	}
	public  T reduce(Function<CompletableFuture,T>safeJoin,T identity, BinaryOperator<T> accumulator){
		if(this.blocking.isParallel()  && results.size()>maxActive.getParallelReduceBatchSize()){
			 return reduceResults(getResults(),safeJoin, identity, accumulator);
		}else if(!this.blocking.isParallel()  && results.size()>maxActive.getMaxActive()){
			return reduceResults(getResults(),safeJoin, identity, accumulator);
		}
		return identity;
	}
	public T reduceResults( Collection<CompletableFuture<T>> results,Function<CompletableFuture, T> safeJoin, T identity,
			BinaryOperator<T> accumulator) {
		Stream<CompletableFuture<T>> stream = results.stream();
		 Stream<CompletableFuture<T>> streamToUse = this.blocking.isParallel() ? stream.parallel() : stream;
		 T result = streamToUse.map(safeJoin)
					.filter(v -> v != MissingValue.MISSING_VALUE).reduce(identity, accumulator);
		getResults().clear();
		return result;
	}
	
}