package com.aol.cyclops.internal.react;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.control.SimpleReact;
import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.data.async.QueueFactories;
import com.aol.cyclops.data.async.QueueFactory;
import com.aol.cyclops.internal.react.stream.EagerStreamWrapper;
import com.aol.cyclops.react.async.subscription.AlwaysContinue;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.types.futurestream.EagerToQueue;
import com.aol.cyclops.types.futurestream.SimpleReactStream;
import com.nurkiewicz.asyncretry.RetryExecutor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

@Wither
@Getter
@Slf4j
@AllArgsConstructor
public class SimpleReactStreamImpl<U> implements SimpleReactStream<U>,EagerToQueue<U>{
	


	private final Optional<Consumer<Throwable>> errorHandler;
	private final EagerStreamWrapper lastActive;
	private final QueueFactory<U> queueFactory;
	private final SimpleReact simpleReact;
	private final Continueable subscription;
	
	
	public SimpleReactStreamImpl(final SimpleReact simpleReact, final Stream<CompletableFuture<U>> stream) {
		this.simpleReact = simpleReact;
		Stream s = stream;
		
		
		this.errorHandler = Optional.of((e) -> log.error(e.getMessage(), e));
		this.lastActive = new EagerStreamWrapper(s,this.errorHandler);
		this.queueFactory = QueueFactories.unboundedQueue();
		this.subscription = new AlwaysContinue();
		
	}
	
	@Override
	public SimpleReactStream<U> withAsync(boolean b) {
		
		return this.withSimpleReact(this.simpleReact.withAsync(b));
	}
	@Override
	public <R> SimpleReactStream<R> thenSync(final Function<? super U,? extends R> fn){
		return SimpleReactStream.super.thenSync(fn);
	}

	@Override
	public <R1,R2> SimpleReactStream<R2> allOf(final Collector<? super U, ?, R1> collector,
            final Function<? super R1,? extends R2> fn) {
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

	public Queue<U> toQueue(){
		return EagerToQueue.super.toQueue();
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