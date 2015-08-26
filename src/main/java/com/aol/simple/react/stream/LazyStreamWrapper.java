package com.aol.simple.react.stream;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.future.FinalPipeline;

@AllArgsConstructor
public class LazyStreamWrapper<U> implements StreamWrapper<U> {
	@Wither
	private final Stream<U> values;
	private final Queue<FastFuture> futures = new LinkedList<>();
	int maxSize = 100;

	private boolean streamCompletableFutures =false;
	
	private FastFuture pipeline;

	public LazyStreamWrapper(Stream values,FastFuture pipeline, boolean streamCompletableFutures){
		
		this.values = values;//values.map(this::nextFuture);//.map( future->returnFuture(future));
		this.pipeline = pipeline;
		this.streamCompletableFutures=streamCompletableFutures;
		
	}
	public LazyStreamWrapper(Stream values){
		
		this.values =values;
		this.pipeline = new FastFuture();
		
	}
	
	public Stream<FastFuture> injectFutures(){
		System.out.println("futures?"+streamCompletableFutures);
		if(streamCompletableFutures)
			return convertCompletableFutures();
		Stream<FastFuture> result = values
									.map(this::nextFutureAndSet);
		
		return result;
	}
	
	public LazyStreamWrapper<U> concat(Stream<U> concatWith){
		return this.withValues(Stream.concat(values, concatWith));
	}
	
	private Stream<FastFuture> convertCompletableFutures(){
		System.out.println("Convert from CompletableFuture");
		return values.map(cf -> nextFuture().populateFromCompletableFuture( (CompletableFuture)cf));
	}
	private FastFuture nextFuture(){
		
		FastFuture f =  futures.poll();
		if(f==null){
			f = pipeline.build();
		}
		return f;
	}
	private FastFuture nextFutureAndSet(Object value){
		
		FastFuture f =  nextFuture();
		f.set(value);
		return f;
	}
	private <T> T returnFuture(FastFuture f){
		T result = (T)f.join();
		if(futures.size()<maxSize)
			futures.offer(f);
		return result;
	}
	//FIXME clean this up (requires separating FutureStream implementations)
	public <R> LazyStreamWrapper<R> operation(Function<FastFuture<U>,FastFuture<R>> action){
		pipeline = action.apply(pipeline);
		return (LazyStreamWrapper)this;
	}
	public LazyStreamWrapper withNewStream(Stream values, BaseSimpleReact simple){
		return new LazyStreamWrapper(values, new FastFuture(),false);
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
