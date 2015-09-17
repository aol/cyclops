package com.aol.simple.react.stream.lazy;

import java.io.BufferedReader;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.BaseStream;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Subscriber;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import com.aol.cyclops.monad.AnyM;
import com.aol.cyclops.sequence.HeadAndTail;
import com.aol.cyclops.sequence.HotStream;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.sequence.future.FutureOperations;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.simple.react.async.Queue;
import com.aol.simple.react.async.factories.QueueFactories;
import com.aol.simple.react.async.factories.QueueFactory;
import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.async.subscription.Continueable;
import com.aol.simple.react.async.subscription.Subscription;
import com.aol.simple.react.collectors.lazy.BatchingCollector;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.config.MaxActive;
import com.aol.simple.react.exceptions.SimpleReactFailedStageException;
import com.aol.simple.react.stream.LazyStreamWrapper;
import com.aol.simple.react.stream.ReactBuilder;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.aol.simple.react.stream.traits.SimpleReactStream;
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
	
	
	
  
	/**
	 * Cancel the CompletableFutures in this stage of the stream
	 */
	public void cancel()	{
		this.subscription.closeAll();
		//also need to mark cancelled =true and check during collection
	}



}
