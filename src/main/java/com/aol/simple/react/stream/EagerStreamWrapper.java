package com.aol.simple.react.stream;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.stream.traits.FutureStream;


@Wither
@AllArgsConstructor
@Builder
public class EagerStreamWrapper implements StreamWrapper{
	@SuppressWarnings("rawtypes")
	private final List<FastFuture> list;
	private final Stream<FastFuture> stream;
	private final boolean eager;
	private final AsyncList async;
	
	public EagerStreamWrapper(List<FastFuture> list){
		this.list = list;
		this.stream = null;
		this.eager = true;
		 async= null;
	}
	public EagerStreamWrapper(AsyncList async){
		this.list = null;
		this.stream = null;
		this.async = async;
		this.eager = true;
	}
	public EagerStreamWrapper(Stream<FastFuture> stream,boolean eager){
		this.stream = stream;
		if(eager){
			list = stream.collect(Collectors.toList());
		}else{
			list = null;
		}
		 async= null;
		this.eager = eager;
	}
	
	public EagerStreamWrapper(FastFuture cf, boolean eager) {
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
	
	
	public EagerStreamWrapper withNewStream(Stream<FastFuture> stream, BaseSimpleReact simple){
		if(simple.getQueueService()==null)
			System.out.println(simple);
		if(!eager)
			return withStream(stream);
		else
			return new EagerStreamWrapper(new AsyncList(stream,simple.getQueueService()));
	}
	public EagerStreamWrapper stream(Function<Stream<FastFuture>,Stream<FastFuture>> action){
		if(async!=null)
			return new EagerStreamWrapper(async.stream(action));
		else if(eager)
			return new EagerStreamWrapper(action.apply(list.stream()),eager);
		
		
		return new EagerStreamWrapper(action.apply(stream),false);
	}
	
	public Stream<FastFuture> stream(){
		if(async!=null)
			return async.async.join().stream();
		if(eager)
			return list.stream();
		else
			return stream;
	}
	
	public List<FastFuture> list(){
		if(async!=null)
			return async.async.join();
		if(eager)
			return list;
		else
			return stream.collect(Collectors.toList());
	}
	
	
	static class AsyncList{
		
		private final Executor service;
		// = Executors.newSingleThreadExecutor();
		private final CompletableFuture<List<FastFuture>> async;
		
		public AsyncList(Stream<FastFuture> stream,Executor service){
			
				
			if(stream instanceof FutureStream)
				async = CompletableFuture.completedFuture(stream.collect(Collectors.toList()));
			else
				async = CompletableFuture.supplyAsync(()-> stream.collect(Collectors.toList()),service);
												
		
		
			this.service= service;
		}
		public AsyncList(CompletableFuture<Stream<FastFuture>> cf,Executor service){
			//use elastic pool to execute asyn
			
				async = cf.thenApplyAsync(st ->st.collect(Collectors.toList()),service);
				this.service= service;
			
		}
		
		
		public AsyncList stream(Function<Stream<FastFuture>,Stream<FastFuture>> action){
			return new AsyncList(async.thenApply(list-> action.apply(list.stream())),service);
			
		}
	}
	
	
	
}
