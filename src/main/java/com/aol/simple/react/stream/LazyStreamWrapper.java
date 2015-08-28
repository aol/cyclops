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

@AllArgsConstructor
public class LazyStreamWrapper<U> implements StreamWrapper<U> {
	@Wither
	private final Stream<U> values;
	
	int maxSize = 100;

	private boolean streamCompletableFutures =false;
	
	private PipelineBuilder pipeline;
	@Getter
	private FuturePool pool = new FuturePool(new ArrayDeque(100), 100);
	public LazyStreamWrapper(Stream values, boolean streamCompletableFutures){
		
		this.values = values;//values.map(this::nextFuture);//.map( future->returnFuture(future));
		this.pipeline = new PipelineBuilder();
		this.streamCompletableFutures=streamCompletableFutures;
		
	}
	
	
	public LazyStreamWrapper(Stream values){
		
		this.values =values;
		this.pipeline = new PipelineBuilder();
		
	}
	
	public Stream<FastFuture> injectFutures(){
		FastFuture f= pipeline.build();
		Function<Object,FastFuture> factory = v -> {
			
			FastFuture next = pool.next( ()->new FastFuture<>(f.getPipeline(), fut->pool.done(fut))); 
			next.set(v);
			return next;
		};
		if(streamCompletableFutures)
			return convertCompletableFutures(f.getPipeline());
		
		Stream<FastFuture> result = values
									.map(factory);
		
		return result;
	}
	
	public LazyStreamWrapper<U> concat(Stream<U> concatWith){
		return this.withValues(Stream.concat(values, concatWith));
	}
	
	private Stream<FastFuture> convertCompletableFutures(FinalPipeline pipeline){
		
		return values.map(cf ->  pool.next( ()->new FastFuture<>(pipeline, fut->pool.done(fut))).populateFromCompletableFuture( (CompletableFuture)cf));
	}
	
	
	
	
	public <R> LazyStreamWrapper<R> operation(Function<PipelineBuilder,PipelineBuilder> action){
		pipeline = action.apply(pipeline);
		return (LazyStreamWrapper)this;
	}
	public <R> LazyStreamWrapper<R> withNewStream(Stream<R> values, BaseSimpleReact simple){
		return new LazyStreamWrapper<R>(values,false);
	}
	
	public LazyStreamWrapper withNewStream(Stream values){
		return new LazyStreamWrapper(values);
	}

	
	public Stream<U> stream() {
		return values;
	}

	
	public LazyStreamWrapper withStream(Stream noType) {
		return this.withValues(noType);
	}
	
	
	
}
