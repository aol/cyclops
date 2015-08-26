package com.aol.simple.react.stream.lazy;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.async.subscription.Subscription;
import com.aol.simple.react.capacity.monitor.LimitingMonitor;
import com.aol.simple.react.collectors.lazy.BatchingCollector;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.ReactBuilder;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.threads.ReactPool;
import com.nurkiewicz.asyncretry.RetryExecutor;

@Wither
@Builder
@Getter
@Slf4j 
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class LazyFutureStreamImpl<U> implements LazyFutureStream<U>{
	
	


	private final Optional<Consumer<Throwable>> errorHandler;
	private final LazyStreamWrapper<U> lastActive;
	private final boolean eager;
	private final Consumer<FastFuture<U>> waitStrategy;
	private final LazyResultConsumer<U> lazyCollector;
	private final QueueFactory<U> queueFactory;
	private final LazyReact simpleReact;
	private final Continueable subscription;
	private final static ReactPool<LazyReact> pool = ReactPool.elasticPool(()->new LazyReact(Executors.newSingleThreadExecutor()));
	private final ParallelReductionConfig parallelReduction;
	private final ConsumerHolder error;
	
	/** FIXME : Potential memory leak creating a new Thread Pool for each Stream **/
	private final ExecutorService publisherExecutor = Executors.newFixedThreadPool(1);
	@AllArgsConstructor
	static class ConsumerHolder{
		volatile Consumer<Throwable> forward;
	}
	
	
	public LazyFutureStreamImpl(LazyReact lazyReact, final Stream<U> stream) {
		
		this.simpleReact = lazyReact;
		
		this.lastActive = new LazyStreamWrapper<>(stream, new FastFuture<>(), lazyReact.isStreamOfFutures());
		this.error =  new ConsumerHolder(a->{});
		this.errorHandler = Optional.of((e) -> { error.forward.accept(e); log.error(e.getMessage(), e);});
		this.eager = false;
		this.waitStrategy = new LimitingMonitor<>(lazyReact.getMaxActive());
		this.lazyCollector = new BatchingCollector<>(this);
		this.queueFactory = QueueFactories.unboundedNonBlockingQueue();
		this.subscription = new Subscription();
		this.parallelReduction = ParallelReductionConfig.defaultValue;
		
		
		
	}
	
	public void forwardErrors(Consumer<Throwable> c){
		error.forward =c;
	}

	
	public LazyReact getPopulator(){
		return pool.nextReactor();
	}
	public void returnPopulator(LazyReact service){
		pool.populate(service);
	}
	
	@Override
	public <R, A> R collect(Collector<? super U, A, R> collector) {
		return block(collector);
	}

	public void close(){
		
	}


	@Override
	public LazyFutureStream<U> withAsync(boolean b) {
		
		return this.withSimpleReact(this.simpleReact.withAsync(b));
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
	public LazyFutureStream<U> withTaskExecutor(Executor e) {
		return this.withSimpleReact(simpleReact.withExecutor(e));
	}



	@Override
	public LazyFutureStream<U> withRetrier(RetryExecutor retry) {
		return this.withSimpleReact(simpleReact.withRetrier(retry));
	}
	@Override
	public LazyFutureStream<U> withLastActive(LazyStreamWrapper w) {
		return new LazyFutureStreamImpl<U>(errorHandler, (LazyStreamWrapper)w, eager, waitStrategy, lazyCollector, queueFactory, simpleReact, subscription, parallelReduction, error);
		
	}
	
	
  
	/**
	 * Cancel the CompletableFutures in this stage of the stream
	 */
	public void cancel()	{
		this.subscription.closeAll();
		//also need to mark cancelled =true and check during collection
	}
}
