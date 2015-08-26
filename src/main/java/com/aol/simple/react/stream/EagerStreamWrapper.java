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

import com.aol.simple.react.stream.traits.FutureStream;

@Wither
@AllArgsConstructor
@Builder
public class EagerStreamWrapper implements StreamWrapper {
	@SuppressWarnings("rawtypes")
	private final List<CompletableFuture> list;
	private final Stream<CompletableFuture> stream;
	private final AsyncList async;

	public EagerStreamWrapper(List<CompletableFuture> list) {
		this.list = list;
		this.stream = null;

		async = null;
	}

	public EagerStreamWrapper(AsyncList async) {
		this.list = null;
		this.stream = null;
		this.async = async;

	}

	public EagerStreamWrapper(Stream<CompletableFuture> stream) {
		this.stream = stream;

		list = stream.collect(Collectors.toList());

		async = null;

	}

	public EagerStreamWrapper(Stream<CompletableFuture> stream, Collector c) {
		this.stream = stream;
		async = null;

		list = (List<CompletableFuture>) stream.collect(c);

	}

	public EagerStreamWrapper(CompletableFuture cf) {
		async = null;
		list = Arrays.asList(cf);
		stream = null;

	}

	public EagerStreamWrapper withNewStream(Stream<CompletableFuture> stream,
			BaseSimpleReact simple) {

		return new EagerStreamWrapper(new AsyncList(stream,
				simple.getQueueService()));
	}

	public EagerStreamWrapper stream(
			Function<Stream<CompletableFuture>, Stream<CompletableFuture>> action) {
		if (async != null)
			return new EagerStreamWrapper(async.stream(action));
		else
			return new EagerStreamWrapper(action.apply(list.stream()));

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

			if (stream instanceof FutureStream)
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