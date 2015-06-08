package com.aol.simple.react.stream.eager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.ThreadPools;
import com.aol.simple.react.stream.traits.EagerFutureStream;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Builder class for EagerFutureStreams
 * 
 * @author johnmcclean
 *
 */
@Builder
@Wither
public class EagerReact extends BaseSimpleReact{
	@Getter
	private final Executor executor;
	@Getter
	private final RetryExecutor retrier;
	@Getter
	private final boolean eager = true;
	
	private final Boolean async;
	
	
	
	public EagerReact(Executor executor, RetryExecutor retrier,
			Boolean async) {
		super();
		this.executor = executor;
		this.retrier = retrier;
		this.async = Optional.ofNullable(async).orElse(true);
	}
	
	/**
	 * Construct a EagerReact builder using standard thread pool.
	 * By default, unless ThreadPools is configured otherwise this will be sized
	 * to the available processors
	 * 
	 * @see ThreadPools#getStandard()
	 */
	public EagerReact(){
		this( ThreadPools.getStandard());
		
	}
	/**
	 * Construct a EagerReact builder with provided Executor
	 * 
	 * @param executor Executor to use
	 */
	public EagerReact(Executor executor) {
		
		this.executor = executor;
		this.retrier = null;
		this.async=true;
		
	}
	
	public <T,R> Function<EagerFutureStream<T>,EagerFutureStream<R>> lift(Function<T,R> fn){
		return efs -> efs.map(v->fn.apply(v));
	}
	public <T1,T2,R> BiFunction<EagerFutureStream<T1>,EagerFutureStream<T2>,EagerFutureStream<R>> lift2(BiFunction<T1,T2,R> fn){
		return (efs1,efs2) -> efs1.flatMap( v1-> (EagerFutureStream)efs2.map(v2->fn.apply(v1,v2)));
	}
	

	@Override
	public <U> EagerFutureStream<U> construct(Stream s,
			List<CompletableFuture> org) {
		return (EagerFutureStream) new EagerFutureStreamImpl<U>( this,s,org);
	}
	/* 
	 * Construct a EagerFutureStream from the provided Stream of completableFutures
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStream(java.util.stream.Stream)
	 */
	@Override
	public <U> EagerFutureStream<U> fromStream(
			Stream<CompletableFuture<U>> stream) {
	
		return (EagerFutureStream)super.fromStream(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public <U> EagerFutureStream<U> of(Stream<U> stream) {
		
		return (EagerFutureStream)super.of(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public EagerFutureStream<Integer> of(IntStream stream) {
		
		return (EagerFutureStream)super.of(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  EagerFutureStream<Double> of(DoubleStream stream) {
		
		return (EagerFutureStream)super.of(stream);
	}
	

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  EagerFutureStream<Long> of(LongStream stream) {
		
		return (EagerFutureStream)super.of(stream);
	}
	/* 
	 * Construct a EagerFutureStream from array
	 * 
	 *	@param array Array that forms basis of Stream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#of(java.lang.Object[])
	 */
	@Override
	public <U> EagerFutureStream<U> of(U... array) {
		
		return (EagerFutureStream)super.of(array);
	}
	
	@SafeVarargs
	public final  <U> EagerFutureStream<U> react(final Supplier<U>... actions) {

		return (EagerFutureStream)super.reactI(actions);

	}
	
	/* 
	 * 
	 * Construct a EagerFutureStream from specified Suppliers. Each Supplier is executed asyncrhonously,
	 * and it's results provided to next phase of the Stream
	 * 
	 *	@param actions Suppliers to execute
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.List)
	 */

	@Override
	public <U> EagerFutureStream<U> react(Collection<Supplier<U>> actions) {
		
		return (EagerFutureStream)super.react(actions);
	}
	
	
	/**
	 * Start a LazyFutureStream from a JDK Iterator
	 * 
	 * @param iterator SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
	 * @param maxTimes Maximum number of iterations
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <U> EagerFutureStream<U> of(Iterator<U> iterator) {
		
		return (EagerFutureStream)super.of(iterator);
	}

	/**
	 * Start a EagerFutureStream from a Collection
	 * 
	 * @param collection - Collection Stream will be formed from
	 *
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <R> EagerFutureStream<R> of(Collection<R> collection) {
		
		return (EagerFutureStream)super.of(collection);
	}



	public boolean isAsync(){
		return async;
	}

	@Override
	public EagerFutureStream<Integer> range(int startInclusive, int endExclusive) {
		// TODO Auto-generated method stub
		return (EagerFutureStream)super.range(startInclusive, endExclusive);
	}

	@Override
	public <U> EagerFutureStream<U> ofIterable(Iterable<U> iter) {
		// TODO Auto-generated method stub
		return (EagerFutureStream)super.of(iter);
	}

	@Override
	public <U> EagerFutureStream<U> react(Stream<Supplier<U>> actions) {
		// TODO Auto-generated method stub
		return (EagerFutureStream)super.react(actions);
	}

	@Override
	public <U> EagerFutureStream<U> react(Iterator<Supplier<U>> actions) {
		// TODO Auto-generated method stub
		return (EagerFutureStream)super.react(actions);
	}

	@Override
	public <U> EagerFutureStream<U> reactIterable(Iterable<Supplier<U>> actions) {
		
		return (EagerFutureStream)super.reactIterable(actions);
	}

	
	

}
