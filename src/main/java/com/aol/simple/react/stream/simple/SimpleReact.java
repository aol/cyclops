package com.aol.simple.react.stream.simple;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.experimental.Builder;
import lombok.Getter;
import lombok.experimental.Wither;

import com.aol.simple.react.RetryBuilder;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.eager.EagerReact;
import com.aol.simple.react.stream.traits.SimpleReactStream;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Builder class for FutureStream
 * 
  * react methods - submit Suppliers to task executor
 * of methods - build Streams directly from data 
 * 
 * @author johnmcclean
 *
 *
 */

@Builder
@Wither
public class SimpleReact  extends BaseSimpleReact{

	@Getter
	private final Executor executor;
	@Getter
	private final RetryExecutor retrier;
	
	
	
	private final Boolean async;
	
	@Override
	public <U> SimpleReactStream<U> construct(Stream s, List<CompletableFuture> originalFutures) {
		return  new SimpleReactStreamImpl<U>( this,s, originalFutures);
	}
	
	
	/**
	 * Construct a SimpleReact builder using standard thread pool.
	 * By default, unless ThreadPools is configured otherwise this will be sized
	 * to the available processors
	 * 
	 * @see ThreadPools#getStandard()
	 */
	public SimpleReact(){
		this( ThreadPools.getStandard());
	}
	
	public SimpleReact(Executor executor, RetryExecutor retrier,
			 Boolean async) {
		super(ThreadPools.getQueueCopyExecutor());
		this.executor = Optional.ofNullable(executor).orElse(
				new ForkJoinPool(Runtime.getRuntime().availableProcessors()));
		this.retrier = retrier;
		
		this.async = Optional.ofNullable(async).orElse(true);
	}
	
	/**
	 * @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 */
	public SimpleReact(Executor executor) {
		super(ThreadPools.getQueueCopyExecutor());
		this.executor = executor;
		this.retrier = null;
		
		this.async =true;
	}
	public SimpleReact(Executor executor,RetryExecutor retrier) {
		super(ThreadPools.getQueueCopyExecutor());
		this.executor = executor;
		this.retrier = retrier;
		
		this.async =true;
	}
	public SimpleReact(Executor executor,RetryExecutor retrier,Executor queueCopier) {
		super(queueCopier);
		this.executor = executor;
		this.retrier = retrier;
		
		this.async =true;
	}
	
	public SimpleReact withQueueCopyExecutor(Executor queueCopyExecutor){
		return new SimpleReact(this.executor,this.retrier,queueCopyExecutor);
	}
	/**
	 * Start a reactive dataflow from a stream of CompletableFutures.
	 * 
	 * @param stream of CompletableFutures that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public <U> SimpleReactStream<U> fromStream(final Stream<CompletableFuture<U>> stream) {
		return super.fromStream(stream);
		
	}

	
	
	/**
	 * 
	 * Start a reactive dataflow with a list of one-off-suppliers
	 * 
	 * @param actions
	 *            List of Suppliers to provide data (and thus events) that
	 *            downstream jobs will react too
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <U> SimpleReactStream<U> react(final Collection<Supplier<U>> actions) {

		return react((Supplier[]) actions.toArray(new Supplier[] {}));
	}
	/**
	 * 
	 * Start a reactive dataflow with a list of one-off-suppliers
	 * 
	 * @param actions
	 *           Stream of Suppliers to provide data (and thus events) that
	 *            downstream jobs will react too
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <U> SimpleReactStream<U> react(final Stream<Supplier<U>> actions) {

		return new SimpleReactStreamImpl<U>(this,actions.map(
				next -> CompletableFuture.supplyAsync(next, executor)),
				null);
		
	}
	/**
	 * 
	 * Start a reactive dataflow with a list of one-off-suppliers
	 * 
	 * @param actions
	 *           Iterator over Suppliers to provide data (and thus events) that
	 *            downstream jobs will react too
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <U> SimpleReactStream<U> react(final Iterator<Supplier<U>> actions) {

		return new SimpleReactStreamImpl<U>(this,StreamSupport.stream(Spliterators.spliteratorUnknownSize(actions, Spliterator.ORDERED),false).map(
				next -> CompletableFuture.supplyAsync(next, executor)),
				null);
		
	}
	/**
	 * 
	 * Start a reactive dataflow with a list of one-off-suppliers
	 * 
	 * @param actions
	 *           Stream of Suppliers to provide data (and thus events) that
	 *            downstream jobs will react too
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <U> SimpleReactStream<U> reactIterable(final Iterable<Supplier<U>> actions) {

		return new SimpleReactStreamImpl<U>(this,StreamSupport.stream(Spliterators.spliteratorUnknownSize(actions.iterator(), Spliterator.ORDERED),false).map(
				next -> CompletableFuture.supplyAsync(next, executor)),
				null);
		
	}
	/**
	 * 
	 * Start a reactive dataflow with an array of one-off-suppliers
	 * 
	 * @param actions Array of Suppliers to provide data (and thus events) that
	 *            downstream jobs will react too
	 * @return Next stage in the reactive flow
	 */
	@SafeVarargs
	public final <U> SimpleReactStream<U> react(final Supplier<U>... actions) {

		return super.react(actions);

	}
	
	
	/**
	 * This internal method has been left protected, so it can be mocked / stubbed as some of the entry points are final
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected <U> SimpleReactStream<U> reactI(final Supplier<U>... actions) {
		
		
			return new SimpleReactStreamImpl<U>(this,Stream.of(actions).map(
				next -> CompletableFuture.supplyAsync(next, executor)),
				null);
		
		
	}
	
	
	
	

	public boolean isAsync(){
		return async;
	}
	
	/**
	 * @return  An Eager SimpleReact instance 
	 *  @see SimpleReact#SimpleReact()
	 */
	public static SimpleReact parallelBuilder() {
		return new SimpleReact();
	}

	/**
	 * Construct a new SimpleReact builder, with a new task executor and retry executor
	 * with configured number of threads 
	 * 
	 * @param parallelism Number of threads task executor should have
	 * @return eager SimpleReact instance
	 */
	public static SimpleReact parallelBuilder(int parallelism) {
		return SimpleReact.builder().executor(new ForkJoinPool(parallelism))
				.retrier(new RetryBuilder().parallelism(parallelism)).build();
	}

	/**
	 * @return new eager SimpleReact builder configured with standard parallel executor
	 * By default this is the ForkJoinPool common instance but is configurable in the ThreadPools class
	 * 
	 * @see ThreadPools#getStandard()
	 * see RetryBuilder#getDefaultInstance()
	 */
	public static SimpleReact parallelCommonBuilder() {
		return SimpleReact.builder().executor(ThreadPools.getStandard()).async(true)
		.retrier(RetryBuilder.getDefaultInstance().withScheduler(ThreadPools.getCommonFreeThreadRetry())).build();
		
	}

	/**
	 * @return new eager SimpleReact builder configured to run on a separate thread (non-blocking current thread), sequentially
	 * New ForkJoinPool will be created
	 */
	public static SimpleReact sequentialBuilder() {
		return SimpleReact.builder().async(false).executor(new ForkJoinPool(1))
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(Executors.newScheduledThreadPool(1))).build();
	}

	/**
	 * @return new eager SimpleReact builder configured to run on a separate thread (non-blocking current thread), sequentially
	 * Common free thread Executor from
	 */
	public static SimpleReact sequentialCommonBuilder() {
		return SimpleReact.builder().async(false).executor(ThreadPools.getCommonFreeThread())
				.retrier(RetryBuilder.getDefaultInstance().withScheduler(ThreadPools.getCommonFreeThreadRetry())).build();
	}
		
	
}
Status API Training Shop Blog About Pricing
© 2015 GitHub, Inc. Terms Privacy Security Contact Help