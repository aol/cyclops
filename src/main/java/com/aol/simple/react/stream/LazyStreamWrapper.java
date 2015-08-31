package com.aol.simple.react.stream;

import java.util.ArrayDeque;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.future.FinalPipeline;
import com.aol.simple.react.async.future.PipelineBuilder;
import com.aol.simple.react.async.future.pool.FuturePool;
import com.aol.simple.react.stream.lazy.LazyReact;

@AllArgsConstructor
public class LazyStreamWrapper<U> implements StreamWrapper<U> {
	@Wither
	private final Stream<U> values;
	@Wither
	private final LazyReact react;
	private PipelineBuilder pipeline;
	private final FuturePool pool;
	
	public LazyStreamWrapper(Stream values, LazyReact react){
		
		this.values = values;
		this.pipeline = new PipelineBuilder(react.isAutoOptimize(),react.getExecutor());
		
		this.react = react;
		if(react.isPoolingActive())
			pool = new FuturePool(new ArrayDeque(react.getMaxActive().getMaxActive()), react.getMaxActive().getMaxActive());
		else
			pool = null;
	}
	
	
		
	
	public Stream<FastFuture> injectFutures(){
		FastFuture f= pipeline.build();
		Function<Object,FastFuture> factory = v -> {
			
			FastFuture next = pool!=null ? pool.next( ()->new FastFuture<>(f.getPipeline(), fut->pool.done(fut))) : new FastFuture<>(f.getPipeline(),0) ; 
			next.set(v);
			return next;
		};
		if(react.isStreamOfFutures())
			return convertCompletableFutures(f.getPipeline());
		
		Stream<FastFuture> result = values
									.map(factory);
		
		return result;
	}
	
	public LazyStreamWrapper<U> concat(Stream<U> concatWith){
		return this.withValues(Stream.concat(values, concatWith));
	}
	
	private Stream<FastFuture> convertCompletableFutures(FinalPipeline pipeline){
		
		return values.map(cf -> buildPool(pipeline).populateFromCompletableFuture( (CompletableFuture)cf));
	}
	
	
	
	
	private FastFuture buildPool(FinalPipeline pipeline) {
		System.out.println("pool is " + pool + " : " + pipeline );
		return  pool!=null ? pool.next( ()->new FastFuture<>(pipeline, fut->pool.done(fut))) : new FastFuture<>(pipeline,0) ; 
	}




	public <R> LazyStreamWrapper<R> operation(Function<PipelineBuilder,PipelineBuilder> action){
		pipeline = action.apply(pipeline);
		return (LazyStreamWrapper)this;
	}
	public <R> LazyStreamWrapper<R> withNewStream(Stream<R> values){
		return new LazyStreamWrapper((Stream)values,react);
	}
	public <R> LazyStreamWrapper<R> withNewStream(Stream<R> values,LazyReact react){
		return new LazyStreamWrapper((Stream)values,react.withStreamOfFutures(false));
	}
	
	

	
	public Stream<U> stream() {
		return values;
	}

	
	public LazyStreamWrapper withStream(Stream noType) {
		return this.withValues(noType);
	}




	public boolean isSequential() {
		return this.pipeline.isSequential();
	}
	
	
	
}
