package com.aol.cyclops.react.stream.lazy;

import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.control.LazyReact;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.react.async.Queue;
import com.aol.cyclops.react.async.factories.QueueFactories;
import com.aol.cyclops.react.async.factories.QueueFactory;
import com.aol.cyclops.react.async.subscription.Continueable;
import com.aol.cyclops.react.async.subscription.Subscription;
import com.aol.cyclops.react.collectors.lazy.BatchingCollector;
import com.aol.cyclops.react.collectors.lazy.LazyResultConsumer;
import com.aol.cyclops.react.config.MaxActive;
import com.aol.cyclops.react.stream.LazyStreamWrapper;
import com.aol.cyclops.react.stream.traits.LazyFutureStream;
import com.aol.cyclops.react.threads.ReactPool;
import com.aol.cyclops.types.stream.HotStream;
import com.aol.cyclops.types.stream.PausableHotStream;
import com.aol.cyclops.util.stream.StreamUtils;
import com.nurkiewicz.asyncretry.RetryExecutor;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

@Wither
@Builder
@Getter
@Slf4j 
@AllArgsConstructor(access=AccessLevel.PRIVATE)
public class LazyFutureStreamImpl<U> implements LazyFutureStream<U>{
	

	private final Optional<Consumer<Throwable>> errorHandler;
	private final LazyStreamWrapper<U> lastActive;
	
	
	private final Supplier<LazyResultConsumer<U>> lazyCollector;
	private final QueueFactory<U> queueFactory;
	private final LazyReact simpleReact;
	private final Continueable subscription;
	private final static ReactPool<LazyReact> pool = ReactPool.elasticPool(()->new LazyReact(Executors.newSingleThreadExecutor()));
	private final ParallelReductionConfig parallelReduction;
	private final ConsumerHolder error;
	
	private final Executor publisherExecutor;
	private final MaxActive maxActive;
	
	
	@AllArgsConstructor
	static class ConsumerHolder{
		volatile Consumer<Throwable> forward;
	}
	
	
	
	public LazyFutureStreamImpl(LazyReact lazyReact, final Stream<U> stream) {
		
		this.simpleReact = lazyReact;
		
		this.lastActive = new LazyStreamWrapper<>(stream, lazyReact);
		this.error =  new ConsumerHolder(a->{});
		this.errorHandler = Optional.of((e) -> { error.forward.accept(e); log.error(e.getMessage(), e);});
		this.lazyCollector = ()->new BatchingCollector<U>(getMaxActive(),this);
		this.queueFactory = QueueFactories.unboundedNonBlockingQueue();
		this.subscription = new Subscription();
		this.parallelReduction = ParallelReductionConfig.defaultValue;
		this.publisherExecutor = lazyReact.getPublisherExecutor();
		this.maxActive = lazyReact.getMaxActive();
		
		
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
		return new LazyFutureStreamImpl<U>(errorHandler, (LazyStreamWrapper)w,  lazyCollector, 
				queueFactory, simpleReact, subscription, parallelReduction, error,this.publisherExecutor,maxActive);
		
	}
	@Override
	public LazyFutureStream<U> maxActive(int max){
		return this.withMaxActive(new MaxActive(max,max));
	}
	
	
	
  
	/**
	 * Cancel the CompletableFutures in this stage of the stream
	 */
	public void cancel()	{
		this.subscription.closeAll();
		//also need to mark cancelled =true and check during collection
	}


	@Override
	public HotStream<U> schedule(String cron, ScheduledExecutorService ex) {
		return ReactiveSeq.<U>fromStream(this.toStream()).schedule(cron, ex);
	}
	@Override
	public HotStream<U> scheduleFixedDelay(long delay,
			ScheduledExecutorService ex) {
		return ReactiveSeq.<U>fromStream(this.toStream()).scheduleFixedDelay(delay, ex);
	}
	@Override
	public HotStream<U> scheduleFixedRate(long rate, ScheduledExecutorService ex) {
		return ReactiveSeq.<U>fromStream(this.toStream()).scheduleFixedRate(rate, ex);
	}
	@Override
	public <X extends Throwable> org.reactivestreams.Subscription forEachX(
			long numberOfElements, Consumer<? super U> consumer) {
		return StreamUtils.forEachX(this, numberOfElements,consumer);
	}
	@Override
	public <X extends Throwable> org.reactivestreams.Subscription forEachXWithError(
			long numberOfElements, Consumer<? super U> consumer,
			Consumer<? super Throwable> consumerError) {
		return StreamUtils.forEachXWithError(this, numberOfElements,consumer, consumerError);
		
	}
	@Override
	public <X extends Throwable> org.reactivestreams.Subscription forEachXEvents(
			long numberOfElements, Consumer<? super U> consumer,
			Consumer<? super Throwable> consumerError, Runnable onComplete) {
		return StreamUtils.forEachXEvents(this, numberOfElements,consumer, consumerError,onComplete);
	}
	@Override
	public <X extends Throwable> void forEachWithError(
			Consumer<? super U> consumerElement,
			Consumer<? super Throwable> consumerError) {
			StreamUtils.forEachWithError(this, consumerElement, consumerError);
	}
	@Override
	public <X extends Throwable> void forEachEvent(
			Consumer<? super U> consumerElement,
			Consumer<? super Throwable> consumerError, Runnable onComplete) {
		
		StreamUtils.forEachEvent(this,consumerElement,
				consumerError,onComplete);
		
	}
	@Override
	public <T> LazyFutureStream<T> unitIterator(Iterator<T> it){
		return simpleReact.from(it);
	}

	@Override
	public LazyFutureStream<U> append(U value) {
		return fromStream(stream().append(value));
	}
	@Override
	public LazyFutureStream<U> prepend(U value) {
		
		return fromStream(stream().prepend(value));
	}
	

	@Override
	public <T> LazyFutureStream<T> unit(T unit) {
		return fromStream(stream().unit(unit));
	}

	@Override
	public HotStream<U> hotStream(Executor e) {
		return stream().hotStream(e);
	}

	@Override
	public HotStream<U> primedHotStream(Executor e) {
		return stream().primedHotStream(e);
	}

	@Override
	public PausableHotStream<U> pausableHotStream(Executor e) {
		return stream().pausableHotStream(e);
	}

	@Override
	public PausableHotStream<U> primedPausableHotStream(Executor e) {
		return stream().primedPausableHotStream(e);
	}

	
}
