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

import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.subscription.AlwaysContinue;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.EagerStreamWrapper;
import com.aol.simple.react.stream.ReactBuilder;
import com.aol.simple.react.stream.lazy.LazyReact;
import com.aol.simple.react.stream.traits.EagerSimpleReactStream;
import com.aol.simple.react.stream.traits.EagerToQueue;
import com.aol.simple.react.threads.ReactPool;
import com.nurkiewicz.asyncretry.RetryExecutor;

@Wither
@Builder
@Getter
@Slf4j
@AllArgsConstructor
public class SimpleReactStreamImpl<U> implements EagerSimpleReactStream<U>,EagerToQueue<U>{
	


	private final Optional<Consumer<Throwable>> errorHandler;
	private final EagerStreamWrapper lastActive;
	private final QueueFactory<U> queueFactory;
	private final SimpleReact simpleReact;
	private final Continueable subscription;
	
	
	public SimpleReactStreamImpl(final SimpleReact simpleReact, final Stream<CompletableFuture<U>> stream) {
		this.simpleReact = simpleReact;
		Stream s = stream;
		this.lastActive = new EagerStreamWrapper(s);
		
		this.errorHandler = Optional.of((e) -> log.error(e.getMessage(), e));
		this.queueFactory = QueueFactories.unboundedQueue();
		this.subscription = new AlwaysContinue();
		
	}
	
	@Override
	public EagerSimpleReactStream<U> withAsync(boolean b) {
		
		return this.withSimpleReact(this.simpleReact.withAsync(b));
	}
	@Override
	public <R> EagerSimpleReactStream<R> thenSync(final Function<U, R> fn){
		return EagerSimpleReactStream.super.thenSync(fn);
	}

	@Override
	public <T, R> EagerSimpleReactStream<R> allOf(final Collector collector,
			final Function<T, R> fn){
		return EagerSimpleReactStream.super.allOf(collector,fn);
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

	public Queue<U> toQueue(){
		return EagerToQueue.super.toQueue();
	}


	@Override
	public EagerSimpleReactStream<U> withTaskExecutor(Executor e) {
		return this.withSimpleReact(simpleReact.withExecutor(e));
	}



	@Override
	public EagerSimpleReactStream<U> withRetrier(RetryExecutor retry) {
		return this.withSimpleReact(simpleReact.withRetrier(retry));
	}
}