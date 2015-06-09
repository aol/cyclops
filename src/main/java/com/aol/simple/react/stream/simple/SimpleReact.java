package com.aol.simple.react.stream.simple;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.traits.SimpleReactStream;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Builder class for FutureStream
 * 
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
	
	public static <T,R> Function<SimpleReactStream<T>,SimpleReactStream<R>> lift(Function<T,R> fn){
		return s1 -> s1.then(fn);
		
	}
	public static <T1,T2,R> BiFunction<SimpleReactStream<T1>,SimpleReactStream<T2>,SimpleReactStream<R>> lift2(BiFunction<T1,T2,R> fn){
		return (s1,s2) -> SimpleReactStream.bind(s1,input1-> s2.then(input2 -> fn.apply(input1,input2)));
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
	
	/**
	 * @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 */
	public SimpleReact(Executor executor) {
		this.executor = executor;
		this.retrier = null;
		
		this.async =true;
	}
	public SimpleReact(Executor executor,RetryExecutor retrier) {
		this.executor = executor;
		this.retrier = retrier;
		
		this.async =true;
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
	
	
	
	public SimpleReact(Executor executor, RetryExecutor retrier,
			 Boolean async) {
		
		this.executor = Optional.ofNullable(executor).orElse(
				new ForkJoinPool(Runtime.getRuntime().availableProcessors()));
		this.retrier = retrier;
		
		this.async = Optional.ofNullable(async).orElse(true);
	}

	public boolean isAsync(){
		return async;
	}
	
	
		
	
}
