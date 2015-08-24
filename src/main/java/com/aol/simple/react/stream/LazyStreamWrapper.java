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
	private final Stream<FastFuture> values;
	private final Queue<FastFuture> futures = new LinkedList<>();
	int maxSize = 100;

	
	@Getter
	private final FastFuture pipeline;
	private FinalPipeline finalPipeline;
	public LazyStreamWrapper(Stream values,FastFuture pipeline){
		this.values = values.map(this::nextFuture);//.map( future->returnFuture(future));
		this.pipeline = pipeline;
		
	}
	
	private FastFuture nextFuture(Object value){
		FastFuture f =  futures.poll();
		if(f==null)
			f = pipeline.build();
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
		return new LazyStreamWrapper(values,action.apply(Stream.of(pipeline)).collect(Collectors.toList()).get(0));
	}
	public StreamWrapper withNewStream(Stream values, BaseSimpleReact simple){
		return this.withValues(values);
	}

	@Override
	public List<FastFuture> list() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Stream<FastFuture> stream() {
		return values;
	}

	@Override
	public StreamWrapper withList(List<FastFuture> list) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public StreamWrapper withStream(Stream<FastFuture> noType) {
		return this.withValues(noType);
	}
	
	
	
}
