package com.aol.simple.react;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.experimental.Builder;
import lombok.experimental.Wither;


@Wither
@AllArgsConstructor
@Builder
class StreamWrapper{
	@SuppressWarnings("rawtypes")
	private final List<CompletableFuture> list;
	private final Stream<CompletableFuture> stream;
	private final boolean eager;
	
	public StreamWrapper(List<CompletableFuture> list){
		this.list = list;
		this.stream = null;
		this.eager = true;
	}
	public StreamWrapper(Stream<CompletableFuture> stream,boolean eager){
		this.stream = stream;
		if(eager){
			list = stream.collect(Collectors.toList());
		}else{
			list = null;
		}
		this.eager = eager;
	}
	public StreamWrapper(Stream<CompletableFuture> stream,Collector c,boolean eager){
		this.stream = stream;
		if(eager){
			list = (List<CompletableFuture>)stream.collect(c);
		}else{
			list = null;
		}
		this.eager = eager;
	}
	public StreamWrapper(CompletableFuture cf, boolean eager) {
		if(eager){
			list = Arrays.asList(cf);
			stream = null;
		}else{
			list = null;
			stream = Stream.of(cf);
		}
		this.eager = eager;
			
	}
	
	public Stream<CompletableFuture> stream(){
		if(eager)
			return list.stream();
		else
			return stream;
	}
	public List<CompletableFuture> list(){
		if(eager)
			return list;
		else
			return stream.collect(Collectors.toList());
	}
	
	StreamWrapper permutate(Stream<CompletableFuture> stream, Collector c){
		return new StreamWrapper(stream,eager);
	}
	
}
