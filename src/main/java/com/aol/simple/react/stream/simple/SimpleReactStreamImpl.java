package com.aol.simple.react.stream.simple;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import com.aol.simple.react.async.AlwaysContinue;
import com.aol.simple.react.async.Continueable;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.capacity.monitor.LimitingMonitor;
import com.aol.simple.react.collectors.lazy.BatchingCollector;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.EagerToQueue;
import com.aol.simple.react.stream.traits.SimpleReactStream;
import com.aol.simple.react.threads.ReactPool;
import com.nurkiewicz.asyncretry.RetryExecutor;

@Wither
@Builder
@Getter
@Slf4j
@AllArgsConstructor
public class SimpleReactStreamImpl<U> implements SimpleReactStream<U>,EagerToQueue<U>{
	


	private final Optional<Consumer<Throwable>> errorHandler;
	private final StreamWrapper lastActive;
	private final boolean eager;
	private final Consumer<CompletableFuture> waitStrategy;
	private final LazyResultConsumer<U> lazyCollector;
	private final QueueFactory<U> queueFactory;
	private final SimpleReact simpleReact;
	private final Continueable subscription;
	private final ReactPool<BaseSimpleReact> pool = ReactPool.elasticPool(()->new LazyReact(Executors.newSingleThreadExecutor()));
	private final List originalFutures;

	
	public SimpleReactStreamImpl(final SimpleReact simpleReact, final Stream<CompletableFuture<U>> stream,
			List<CompletableFuture> originalFutures) {
		this.simpleReact = simpleReact;
		Stream s = stream;
		this.lastActive = new StreamWrapper(s, true);
		if(simpleReact.isEager())
			this.originalFutures = originalFutures!=null ? originalFutures : this.lastActive.list();
		else
			this.originalFutures =null;
		this.errorHandler = Optional.of((e) -> log.error(e.getMessage(), e));
		this.eager = true;
		this.waitStrategy = new LimitingMonitor();
		this.lazyCollector = new BatchingCollector<>(this);
		this.queueFactory = QueueFactories.unboundedQueue();
		this.subscription = new AlwaysContinue();

		
	}
	public BaseSimpleReact getPopulator(){
		return pool.nextReactor();
	}
	public void returnPopulator(BaseSimpleReact service){
		pool.populate(service);
	}
	@Override
	public SimpleReactStream<U> withAsync(boolean b) {
		
		return this.withSimpleReact(this.simpleReact.withAsync(b));
	}
	@Override
	public <R> SimpleReactStream<R> thenSync(final Function<U, R> fn){
		return SimpleReactStream.super.thenSync(fn);
	}

	@Override
	public <T, R> SimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn){
		return SimpleReactStream.super.allOf(collector,fn);
	}

	@Override
	public Executor getTaskExecutor() {
		return this.simpleReact.getExecutor();
	}



	@Override
	public RetryExecutor getRetrier() {
		return this.simpleReact.getRetrier();
	}



	@Override
	public boolean isAsync() {
		return this.simpleReact.isAsync();
	}



	@Override
	public SimpleReactStream<U> withTaskExecutor(Executor e) {
		return this.withSimpleReact(simpleReact.withExecutor(e));
	}



	@Override
	public SimpleReactStream<U> withRetrier(RetryExecutor retry) {
		return this.withSimpleReact(simpleReact.withRetrier(retry));
	}
}
