package com.aol.simple.react.stream.eager;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Builder;
import lombok.experimental.Wither;

import com.aol.simple.react.generators.Generator;
import com.aol.simple.react.generators.ReactIterator;
import com.aol.simple.react.stream.BaseSimpleReact;
import com.aol.simple.react.stream.ThreadPools;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * Builder class for EagerFutureStreams
 * 
 * @author johnmcclean
 *
 */
@Builder
@Wither
@AllArgsConstructor
public class EagerReact extends BaseSimpleReact{
	@Getter
	private final ExecutorService executor;
	@Getter
	private final RetryExecutor retrier;
	@Getter
	private final boolean eager = true;
	
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
	 * Construct a EagerReact builder with provided ExecutorService
	 * 
	 * @param executor ExecutorService to use
	 */
	public EagerReact(ExecutorService executor) {
		this.executor = executor;
		this.retrier = null;
		
	}

	@Override
	public <U> EagerFutureStream<U> construct(Stream s,
			ExecutorService executor, RetryExecutor retrier, boolean eager,List<CompletableFuture> org) {
		return (EagerFutureStream) new EagerFutureStreamImpl<U>( s,executor, retrier);
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
	public <U> EagerFutureStream<U> fromStreamWithoutFutures(Stream<U> stream) {
		
		return (EagerFutureStream)super.fromStreamWithoutFutures(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public EagerFutureStream<Integer> fromPrimitiveStream(IntStream stream) {
		
		return (EagerFutureStream)super.fromPrimitiveStream(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  EagerFutureStream<Double> fromPrimitiveStream(DoubleStream stream) {
		
		return (EagerFutureStream)super.fromPrimitiveStream(stream);
	}
	

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  EagerFutureStream<Long> fromPrimitiveStream(LongStream stream) {
		
		return (EagerFutureStream)super.fromPrimitiveStream(stream);
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
	public <U> EagerFutureStream<U> react(List<Supplier<U>> actions) {
		
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
	public <U> EagerFutureStream<U> react(Iterator<U> iterator, int maxTimes) {
		
		return (EagerFutureStream)super.react(iterator, maxTimes);
	}

	/**
	 * Start a EagerFutureStream from a Collection
	 * 
	 * @param collection - Collection Stream will be formed from
	 *
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <R> EagerFutureStream<R> reactToCollection(Collection<R> collection) {
		
		return (EagerFutureStream)super.reactToCollection(collection);
	}

	/**
	 * Start a EagerFutureStream from a single Supplier, which will be executed repeatedly according to rules defined by the generator.
	 * 
	 * Example : 
	 * To execute the same Supplier 4 times use :
	 * <code>
	 * List&lt;String&gt; strings = new EagerReact()
				.&lt;Integer&gt; react(() -&gt; count++ ,EagerReact.times(4))
	 * </code>
	 * To skip the first 5 iterations and take the next 5
	 *  * <code>
	 * List&lt;String&gt; strings = new EagerReact()
				.&lt;Integer&gt; react(() -&gt; count++ ,EagerReact.times(5).offset(5))
	 * </code>
	 * 
	 * The supplier will be called 10 times, in the above example, but only the last 5 results will be passed into the 
	 * reactive dataflow.
	 * 
	 * @param s Supplier to provide data (and thus events) that
	 *            downstream jobs will react too
	 * @param t Generator implementation that will determine how the Supplier is executed
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <U> EagerFutureStream<U> react(Supplier<U> s, Generator t) {
		
		return (EagerFutureStream)super.react(s, t);
	}

	/**
	 * Start an EagerFutureStream that calls the supplied function iteratively, with each output, feeding into the next input
	 *
	 * Example :-
	 * 
	 * <code>
	 * List&lt;Integer&gt; results = new EagerReact()
				.&lt;Integer&gt; react((input) -&gt; input + 1,iterate(0).times(1).offset(10))
	 * </code>
	 * 
	 * 
	 * @param f Function to be called iteratively
	 * @param t Iterator that manages function call
	 * @return Next stage in the reactive flow
	 */

	@Override
	public <U> EagerFutureStream<U> react(Function<U, U> f, ReactIterator<U> t) {
		
		return (EagerFutureStream)super.react(f, t);
	}

}
