package com.aol.simple.react.stream.traits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import com.aol.simple.react.collectors.lazy.EmptyCollector;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.exceptions.SimpleReactProcessingException;
import com.aol.simple.react.stream.Runner;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.ThreadPools;
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
		new SimpleReact(e).react(() -> run(new NonCollector()));
		

	}
	/*
	default void run(ExecutorService e,Runnable r) {
		new SimpleReact(e).react(() -> new Runner(r).run(getLastActive(),
					new EmptyCollector(getLazyCollector().getMaxActive())));

	}
	 */
	default void runThread(Runnable r) {
		new Thread(() -> new Runner(r).run(getLastActive(),new EmptyCollector(getLazyCollector().getMaxActive()))).start();

	}
	default Continuation runContinuation(Runnable r) {
		return new Runner(r).runContinuations(getLastActive(),new EmptyCollector(getLazyCollector().getMaxActive()));

	}
	/**
	 * Trigger a lazy stream
	 */
	default void runOnCurrent() {
		
		
		run(new NonCollector());

	}
	/**
	 * Trigger a lazy stream
	 */
	default void run() {
		run(ThreadPools.getLazyExecutor());

	}

	/**
	 * Trigger a lazy stream and return the results in the Collection created by
	 * the collector
	 * 
	 * @param collector
	 *            Supplier that creates a collection to store results in
	 * @return Collection of results
	 */
	default <A,R> R run(Collector<U,A,R> collector) {

		
		Optional<LazyResultConsumer<U>> batcher = collector.supplier().get() != null ? Optional
				.of(getLazyCollector().withResults( new ArrayList<>())) : Optional.empty();

		try {
			this.getLastActive().stream().forEach(n -> {

				batcher.ifPresent(c -> c.accept(n));
				this.getWaitStrategy().accept(n);
			});
		} catch (SimpleReactProcessingException e) {
			
		}
		if (collector.supplier().get() == null)
			return null;
		return (R)batcher.get().getResults().stream()
									.map(cf -> BlockingStream.getSafe(cf,getLazyCollector().getBlocking().getErrorHandler()))
									.collect((Collector)collector);
		

	}
}