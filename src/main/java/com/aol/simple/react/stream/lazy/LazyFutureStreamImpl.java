package com.aol.simple.react.stream.lazy;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;
import lombok.extern.slf4j.Slf4j;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.async.QueueFactories;
import com.aol.simple.react.async.QueueFactory;
import com.aol.simple.react.capacity.monitor.LimitingMonitor;
import com.aol.simple.react.collectors.lazy.BatchingCollector;
import com.aol.simple.react.collectors.lazy.LazyResultConsumer;
import com.aol.simple.react.stream.FutureStreamImpl;
import com.aol.simple.react.stream.StreamWrapper;
import com.aol.simple.react.stream.api.FutureStream;
import com.aol.simple.react.stream.eager.EagerFutureStreamImpl;
import com.aol.simple.react.stream.eager.EagerFutureStreamImpl.EagerFutureStreamImplBuilder;
import com.nurkiewicz.asyncretry.RetryExecutor;

@Wither
@Builder
@Getter
@Slf4j
@AllArgsConstructor
public class LazyFutureStreamImpl<U> extends FutureStreamImpl<U> implements LazyFutureStream<U>{
	
	

	private final ExecutorService taskExecutor;
	private final RetryExecutor retrier;
	private final Optional<Consumer<Throwable>> errorHandler;
	private final StreamWrapper lastActive;
	private final boolean eager;
	private final Consumer<CompletableFuture> waitStrategy;
	private final LazyResultConsumer<U> lazyCollector;
	private final QueueFactory<U> queueFactory;
	
	LazyFutureStreamImpl(final Stream<CompletableFuture<U>> stream,
			final ExecutorService executor, final RetryExecutor retrier) {
		this.taskExecutor = Optional.ofNullable(executor).orElse(
				new ForkJoinPool(Runtime.getRuntime().availableProcessors()));
		Stream s = stream;
		this.lastActive = new StreamWrapper(s, false);
		this.errorHandler = Optional.of((e) -> log.error(e.getMessage(), e));
		this.eager = false;
		this.retrier = Optional.ofNullable(retrier).orElse(
				RetryBuilder.getDefaultInstance());
		this.waitStrategy = new LimitingMonitor();
		this.lazyCollector = new BatchingCollector<>();
		this.queueFactory = QueueFactories.boundedQueue(1000);
	}

	
	@Override
	public <R, A> R collect(Collector<? super U, A, R> collector) {
		return block(collector);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.simple.react.stream.FutureStreamImpl#then(java.util.function.Function)
	 */
	@Override
	public <R> LazyFutureStream<R> then(Function<U, R> fn) {
		// TODO Auto-generated method stub
		return (LazyFutureStream)super.then(fn);
	}

	/* (non-Javadoc)
	 * @see com.aol.simple.react.stream.FutureStreamImpl#flatMap(java.util.function.Function)
	 */
	@Override
	public <R> LazyFutureStream<R> flatMap(
			Function<? super U, ? extends Stream<? extends R>> flatFn) {
		// TODO Auto-generated method stub
		return (LazyFutureStream)super.flatMap(flatFn);
	}

	/* (non-Javadoc)
	 * @see com.aol.simple.react.stream.FutureStreamImpl#retry(java.util.function.Function)
	 */
	@Override
	public <R> LazyFutureStream<R> retry(Function<U, R> fn) {
		
		return (LazyFutureStream)super.retry(fn);
	}



	/* (non-Javadoc)
	 * @see com.aol.simple.react.stream.FutureStreamImpl#allOf(java.util.stream.Collector, java.util.function.Function)
	 */
	@Override
	public <T, R> FutureStream<R> allOf(Collector collector, Function<T, R> fn) {
		// TODO Auto-generated method stub
		return (LazyFutureStream)super.allOf(collector, fn);
	}


	/* (non-Javadoc)
	 * @see com.aol.simple.react.stream.FutureStreamImpl#fromStream(java.util.stream.Stream)
	 */
	@Override
	public <R> LazyFutureStream<R> fromStream(Stream<R> stream) {
		// TODO Auto-generated method stub
		return (LazyFutureStream)super.fromStream(stream);
	}

	/* (non-Javadoc)
	 * @see com.aol.simple.react.stream.FutureStreamImpl#fromStreamCompletableFuture(java.util.stream.Stream)
	 */
	@Override
	public <R> LazyFutureStream<R> fromStreamCompletableFuture(
			Stream<CompletableFuture<R>> stream) {
		
		return (LazyFutureStream)super.fromStreamCompletableFuture(stream);
	}
	
	
  
	
}
