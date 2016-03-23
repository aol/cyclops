package com.aol.cyclops.internal.react.stream;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.types.futurestream.BlockingStreamHelper;
import com.aol.cyclops.types.futurestream.SimpleReactStream;
import com.aol.cyclops.util.ExceptionSoftener;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

@Wither
@AllArgsConstructor
public class EagerStreamWrapper implements StreamWrapper {
	@SuppressWarnings("rawtypes")
	private final List<CompletableFuture> list;
	private final Stream<CompletableFuture> stream;
	private final AsyncList async;
	private final Optional<Consumer<Throwable>> errorHandler;

	
	public EagerStreamWrapper(List<CompletableFuture> list,Optional<Consumer<Throwable>> errorHandler) {
		this.list = list;
		this.stream = null;
		this.errorHandler = errorHandler;
		async = null;
	}

	public EagerStreamWrapper(AsyncList async,Optional<Consumer<Throwable>> errorHandler) {
		this.list = null;
		this.stream = null;
		this.async = async;
		this.errorHandler = errorHandler;
	}

	public EagerStreamWrapper(Stream<CompletableFuture> stream,Optional<Consumer<Throwable>> errorHandler) {
		this.stream = stream;

		list = collect(stream,Collectors.toList(),errorHandler);
		this.errorHandler = errorHandler;
		async = null;

	}

	public EagerStreamWrapper(Stream<CompletableFuture> stream, Collector c,Optional<Consumer<Throwable>> errorHandler) {
		this.stream = stream;
		async = null;
		this.errorHandler = errorHandler;
		list =  collect(stream,c,errorHandler);

	}
	public EagerStreamWrapper collect(){
	    if(list!=null)
	        return new EagerStreamWrapper(list.stream(),this.errorHandler);
	    return new EagerStreamWrapper(stream,this.errorHandler);
	}
	static  List<CompletableFuture> collect(Stream<CompletableFuture> stream,Collector c,Optional<Consumer<Throwable>> errorHandler){
	   
	    Function<Throwable,Object> captureFn = t->{BlockingStreamHelper.captureUnwrap((CompletionException)t, errorHandler); throw ExceptionSoftener.throwSoftenedException(t);};
	    if(errorHandler.isPresent())
	        return (List<CompletableFuture>)stream
	                                .map(cf->cf.exceptionally(captureFn)).filter(cf->!cf.isCompletedExceptionally()).collect(c);
	   
	    return (List<CompletableFuture>)stream.filter(cf->cf.isCompletedExceptionally()).collect(c);
       
	}

	
	public EagerStreamWrapper(CompletableFuture cf,Optional<Consumer<Throwable>> errorHandler) {
		async = null;
		list = collect(Stream.of(cf),Collectors.toList(),errorHandler);
		this.errorHandler = errorHandler;
		stream = null;

	}

	public EagerStreamWrapper withNewStream(Stream<CompletableFuture> stream,
			SimpleReact simple) {

		return new EagerStreamWrapper(new AsyncList(stream,
				simple.getQueueService()),this.errorHandler);
	}

	public EagerStreamWrapper stream(
			Function<Stream<CompletableFuture>, Stream<CompletableFuture>> action) {
		if (async != null)
			return new EagerStreamWrapper(async.stream(action),this.errorHandler);
		else
			return new EagerStreamWrapper(action.apply(list.stream()),this.errorHandler);

	}

	public Stream<CompletableFuture> stream() {
		if (async != null)
			return async.async.join().stream();

		return list.stream();

	}

	public List<CompletableFuture> list() {
		if (async != null)
			return async.async.join();

		return list;
	}

	static class AsyncList {

		private final Executor service;
		// = Executors.newSingleThreadExecutor();
		private final CompletableFuture<List<CompletableFuture>> async;

		public AsyncList(Stream<CompletableFuture> stream, Executor service) {

			if (stream instanceof SimpleReactStream)
				async = CompletableFuture.completedFuture(stream
						.collect(Collectors.toList()));
			else
				async = CompletableFuture.supplyAsync(
						() -> stream.collect(Collectors.toList()), service);

			this.service = service;
		}

		public AsyncList(CompletableFuture<Stream<CompletableFuture>> cf,
				Executor service) {
			// use elastic pool to execute asyn

			async = cf.thenApplyAsync(st -> st.collect(Collectors.toList()),
					service);
			this.service = service;

		}

		public AsyncList stream(
				Function<Stream<CompletableFuture>, Stream<CompletableFuture>> action) {
			return new AsyncList(async.thenApply(list -> action.apply(list
					.stream())), service);

		}
	}

}