package com.aol.simple.react.stream.lazy;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
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
import com.aol.simple.react.stream.BaseLazySimpleReact;
import com.aol.simple.react.stream.ThreadPools;
import com.nurkiewicz.asyncretry.RetryExecutor;

/**
 * 
 * Builder class for LazyFutureStreams
 * 
 * @author johnmcclean
 *
 */
@Builder
@Wither
@AllArgsConstructor
public class LazyReact extends BaseLazySimpleReact {
	
	@Getter
	private final ExecutorService executor;
	@Getter
	private final RetryExecutor retrier;
	@Getter
	private final boolean eager = false;
	
	
	

	/**
	 * Construct a LazyReact builder using standard thread pool.
	 * By default, unless ThreadPools is configured otherwise this will be sized
	 * to the available processors
	 * 
	 * @see ThreadPools#getStandard()
	 */
	public LazyReact(){
		
		this( ThreadPools.getStandard());
		
	}
	/**
	 * Construct a LazyReact builder with provided ExecutorService
	 * 
	 * @param executor ExecutorService to use
	 */
	public LazyReact(ExecutorService executor) {
		
		this.executor = executor;
		this.retrier = null;
		
		
		
	}
	
	
	@Override
	public <U> LazyFutureStream<U> construct(Stream s,
			ExecutorService executor, RetryExecutor retrier, boolean eager,List<CompletableFuture> org) {
		
		return (LazyFutureStream) new LazyFutureStreamImpl<U>( s,executor, retrier,org);

	}
	

	/* 
	 * Construct a LazyFutureStream from the provided Stream of completableFutures
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStream(java.util.stream.Stream)
	 */
	@Override
	public <U> LazyFutureStream<U> fromStream(
			Stream<CompletableFuture<U>> stream) {
	
		return (LazyFutureStream)super.fromStream(stream);
	}

	@SafeVarargs
	public final <U> LazyFutureStream<U> react(final Supplier<U>... actions) {

		return (LazyFutureStream)super.reactI(actions);

	}
	
	/* 
	 *  Construct a LazyFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public <U> LazyFutureStream<U> fromStreamWithoutFutures(Stream<U> stream) {
		
		return (LazyFutureStream)super.fromStreamWithoutFutures(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public  LazyFutureStream<Integer> fromPrimitiveStream(IntStream stream) {
		
		return (LazyFutureStream)super.fromPrimitiveStream(stream);
	}

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  LazyFutureStream<Double> fromPrimitiveStream(DoubleStream stream) {
		
		return (LazyFutureStream)super.fromPrimitiveStream(stream);
	}
	

	/* 
	 *  Construct a EagerFutureStream from the provided Stream, Stream will be mapped to a Stream of CompeltableFutures internally
	 * 
	 *	@param stream Stream that serves as input to LazyFutureStream
	 *	@return EagerFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#fromStreamWithoutFutures(java.util.stream.Stream)
	 */
	@Override
	public  LazyFutureStream<Long> fromPrimitiveStream(LongStream stream) {
		
		return (LazyFutureStream)super.fromPrimitiveStream(stream);
	}
	
	/* 
	 * Construct a LazyFutureStream from array
	 * 
	 *	@param array Array that forms basis of Stream
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#of(java.lang.Object[])
	 */
	@Override
	public <U> LazyFutureStream<U> of(U... array) {
		
		return (LazyFutureStream)super.of(array);
	}

	/* 
	 * 
	 * Construct a LazyFutureStream from specified Suppliers. Each Supplier is executed asyncrhonously,
	 * and it's results provided to next phase of the Stream
	 * 
	 *	@param actions Suppliers to execute
	 *	@return LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#react(java.util.List)
	 */
	@Override
	public <U> LazyFutureStream<U> react(List<Supplier<U>> actions) {
		
		return (LazyFutureStream)super.react(actions);
	}

	/**
	 * Start a LazyFutureStream from a JDK Iterator
	 * 
	 * @param iterator SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
	 * @param maxTimes Maximum number of iterations
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <U> LazyFutureStream<U> react(Iterator<U> iterator, int maxTimes) {
		
		return (LazyFutureStream)super.react(iterator, maxTimes);
	}

	/**
	 * Start a LazyFutureStream from a Collection
	 * 
	 * @param collection - Collection Stream will be formed from
	 *
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <R> LazyFutureStream<R> reactToCollection(Collection<R> collection) {
		
		return (LazyFutureStream)super.reactToCollection(collection);
	}

	/**
	 * Start a LazyFutureStream from a single Supplier, which will be executed repeatedly according to rules defined by the generator.
	 * 
	 * Example : 
	 * To execute the same Supplier 4 times use :
	 * <code>
	 * List&lt;String&gt; strings = new LazyReact()
				.&lt;Integer&gt; react(() -&gt; count++ ,LazyReact.times(4))
	 * </code>
	 * To skip the first 5 iterations and take the next 5
	 *  * <code>
	 * List&lt;String&gt; strings = new LazyReact()
				.&lt;Integer&gt; react(() -&gt; count++ ,LazyReact.times(5).offset(5))
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
	public <U> LazyFutureStream<U> react(Supplier<U> s, Generator t) {
		
		return (LazyFutureStream)super.react(s, t);
	}

	/**
	 * Generate an infinite LazyFutureStream
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	@Override
	public <U> LazyFutureStream<U> reactInfinitely(Supplier<U> s) {
		
		return (LazyFutureStream)super.reactInfinitely(s);
	}
	/**
	 * Generate an infinite reactive flow. Requires a lazy flow. Supplier may be executed multiple times in parallel asynchronously by populating thread.
	 * Active CompletableFutures may grow rapidly.
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	public <U> LazyFutureStream< U> reactInfinitelyAsync(final Supplier<U> s) {
		return (LazyFutureStream<U>)super.reactInfinitelyAsync(s);
		

	}
	/* 
	 * Create an Infinite LazyFutureStream using provided params
	 * 
	 *	@param seed Initial value
	 *	@param f Generates subsequent values for the stream
	 *	@return Infinite LazyFutureStream
	 * @see com.aol.simple.react.stream.BaseSimpleReact#iterateInfinitely(java.lang.Object, java.util.function.UnaryOperator)
	 */
	@Override
	public <U> LazyFutureStream<U> iterateInfinitely(U seed, UnaryOperator<U> f) {
	
		return (LazyFutureStream)super.iterateInfinitely(seed, f);
	}

	/**
	 * Start a LazyFutureStream that calls the supplied function iteratively, with each output, feeding into the next input
	 *
	 * Example :-
	 * 
	 * <code>
	 * List&lt;Integer&gt; results = new LazyReact()
				.&lt;Integer&gt; react((input) -&gt; input + 1,iterate(0).times(1).offset(10))
	 * </code>
	 * 
	 * 
	 * @param f Function to be called iteratively
	 * @param t Iterator that manages function call
	 * @return Next stage in the reactive flow
	 */
	@Override
	public <U> LazyFutureStream<U> react(Function<U, U> f, ReactIterator<U> t) {
		
		return (LazyFutureStream)super.react(f, t);
	}

	@Override
	protected <U> LazyFutureStream<U> reactI(Supplier<U>... actions) {
		
		return (LazyFutureStream)super.reactI(actions);
	}
	
	

}
