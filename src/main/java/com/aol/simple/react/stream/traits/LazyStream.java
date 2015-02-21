package com.aol.simple.react.stream.traits;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.Runner;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.simple.SimpleReact;

public interface LazyStream<U> {
	
	abstract StreamWrapper getLastActive();
	abstract LazyResultConsumer<U> getLazyCollector();
	Consumer<CompletableFuture> getWaitStrategy();
	
	/**
	 * Trigger a lazy stream as a task on the provided ExecutorService
	 * 
	 * @param e
	 *            Executor service to trigger lazy stream on (Stream
	 *            CompletableFutures will use ExecutorService associated with
	 *            this Stage may not be the same one).
	 * 
	 * 
	 */
	default void run(ExecutorService e) {
		new SimpleReact(e).react(() -> run(() -> null));
		

	}
	default void run(ExecutorService e,Runnable r) {
		new SimpleReact(e).react(() -> new Runner(r).run(getLastActive(),new EmptyCollector(getLazyCollector().getMaxActive())));

	}
	/**
	 * Trigger a lazy stream
	 */
	default void runOnCurrent() {
		run(() -> null);

	}
	/**
	 * Trigger a lazy stream
	 */
	default void run() {
		run(new ForkJoinPool(1));

	}

	/**
	 * Trigger a lazy stream and return the results in the Collection created by
	 * the collector
	 * 
	 * @param collector
	 *            Supplier that creates a collection to store results in
	 * @return Collection of results
	 */
	default <C extends Collection<U>> C run(Supplier<C> collector) {

		C result = (C) collector.get();

		Optional<LazyResultConsumer<U>> batcher = result != null ? Optional
				.of(getLazyCollector().withResults(result)) : Optional.empty();

		try {
			this.getLastActive().stream().forEach(n -> {

				batcher.ifPresent(c -> c.accept(n));
				this.getWaitStrategy().accept(n);
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		if (result == null)
			return null;
		return (C) batcher.get().getResults();

	}
}
