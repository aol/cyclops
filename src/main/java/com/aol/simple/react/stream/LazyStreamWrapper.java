package com.aol.simple.react.stream;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.future.FinalPipeline;

@AllArgsConstructor
public class LazyStreamWrapper implements StreamWrapper{
	@Wither
	private final Stream values;
	private final Queue<FastFuture> futures = new LinkedList<>();
	int maxSize = 100;

	
	@Getter
	private FastFuture pipeline;
//	private FinalPipeline finalPipeline;
	public LazyStreamWrapper(Stream values,FastFuture pipeline){
		
		this.values = values;//values.map(this::nextFuture);//.map( future->returnFuture(future));
		this.pipeline = pipeline;
		
	}
	public LazyStreamWrapper(Stream values){
		
		this.values =values;
		this.pipeline = new FastFuture();
		
	}
	
	public Stream<FastFuture> injectFutures(){
		
		Stream<FastFuture> result = values.peek(System.out::println)
									.map(this::nextFuture);
		
		System.out.println("Injected!");
		return result;
	}
	
	private FastFuture nextFuture(Object value){
		
		FastFuture f =  futures.poll();
		if(f==null){
			f = pipeline.build();
		}
		f.set(value);
		return f;
	}
	private <T> T returnFuture(FastFuture f){
		T result = (T)f.join();
		if(futures.size()<maxSize)
			futures.offer(f);
		return result;
	}
	public StreamWrapper stream(Function<Stream<FastFuture>,Stream<FastFuture>> action){
		pipeline = action.apply(Stream.of(pipeline)).collect(Collectors.toList()).get(0);
		return this;
	}
	public StreamWrapper withNewStream(Stream values, BaseSimpleReact simple){
		return new LazyStreamWrapper(values, new FastFuture());
	}
	
	public StreamWrapper withNewStream(Stream values){
		return new LazyStreamWrapper(values);
	}

	@Override
	public List<FastFuture> list() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream stream() {
		return values;
	}

	@Override
	public StreamWrapper withList(List<FastFuture> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StreamWrapper withStream(Stream noType) {
		return this.withValues(noType);
	}
	
	
	
}
