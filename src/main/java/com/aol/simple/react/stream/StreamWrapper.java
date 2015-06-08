package com.aol.simple.react.stream;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.stream.traits.FutureStream;


@Wither
@AllArgsConstructor
@Builder
public class StreamWrapper{
	@SuppressWarnings("rawtypes")
	private final List<CompletableFuture> list;
	private final Stream<CompletableFuture> stream;
	private final boolean eager;
	private final AsyncList async;
	
	public StreamWrapper(List<CompletableFuture> list){
		this.list = list;
		this.stream = null;
		this.eager = true;
		 async= null;
	}
	public StreamWrapper(AsyncList async){
		this.list = null;
		this.stream = null;
		this.async = async;
		this.eager = true;
	}
	public StreamWrapper(Stream<CompletableFuture> stream,boolean eager){
		this.stream = stream;
		if(eager){
			list = stream.collect(Collectors.toList());
		}else{
			list = null;
		}
		 async= null;
		this.eager = eager;
	}
	public StreamWrapper(Stream<CompletableFuture> stream,Collector c,boolean eager){
		this.stream = stream;
		 async=null;
		if(eager){
			list = (List<CompletableFuture>)stream.collect(c);
		}else{
			list = null;
		}
		this.eager = eager;
	}
	public StreamWrapper(CompletableFuture cf, boolean eager) {
		 async= null;
		if(eager){
			list = Arrays.asList(cf);
			stream = null;
		}else{
			list = null;
			stream = Stream.of(cf);
		}
		this.eager = eager;
			
	}
	
	
	public StreamWrapper withNewStream(Stream<CompletableFuture> stream){
		if(!eager)
			return withStream(stream);
		else
			return new StreamWrapper(new AsyncList(stream));
	}
	public StreamWrapper stream(Function<Stream<CompletableFuture>,Stream<CompletableFuture>> action){
		if(async!=null)
			return new StreamWrapper(async.stream(action));
		else if(eager)
			return new StreamWrapper(action.apply(list.stream()),eager);
		
		
		return new StreamWrapper(action.apply(stream),false);
	}
	
	public Stream<CompletableFuture> stream(){
		if(async!=null)
			return async.async.join().stream();
		if(eager)
			return list.stream();
		else
			return stream;
	}
	
	public List<CompletableFuture> list(){
		if(async!=null)
			return async.async.join();
		if(eager)
			return list;
		else
			return stream.collect(Collectors.toList());
	}
	
	
	static class AsyncList{
		private final static Executor service = Executors.newSingleThreadExecutor();
		private final CompletableFuture<List<CompletableFuture>> async;
		
		public AsyncList(Stream<CompletableFuture> stream){
				
			if(stream instanceof FutureStream)
				async = CompletableFuture.completedFuture(stream.collect(Collectors.toList()));
			else
				async = CompletableFuture.supplyAsync(()-> stream.collect(Collectors.toList()),service);
												
		
		
	
		}
		public AsyncList(CompletableFuture<Stream<CompletableFuture>> stream){
			
			async = stream.thenApplyAsync(st ->st.collect(Collectors.toList()),service);
			
		}
		
		
		public AsyncList stream(Function<Stream<CompletableFuture>,Stream<CompletableFuture>> action){
			return new AsyncList(async.thenApply(list-> action.apply(list.stream())));
			
		}
	}
	
	
	
}
