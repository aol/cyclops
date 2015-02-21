package com.aol.simple.react.stream;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import lombok.Getter;
import lombok.experimental.Builder;

import com.aol.simple.react.generators.Generator;
import com.aol.simple.react.generators.ParallelGenerator;
import com.aol.simple.react.generators.ReactIterator;
import com.aol.simple.react.generators.SequentialIterator;
import com.aol.simple.react.stream.api.SimpleReactStream;
import com.aol.simple.react.stream.simple.SimpleReact;
import com.google.common.annotations.VisibleForTesting;
import com.nurkiewicz.asyncretry.RetryExecutor;


public abstract class BaseSimpleReact {

	protected abstract ExecutorService getExecutor();
	protected abstract boolean isEager();
	protected abstract  RetryExecutor getRetrier();
	
	protected abstract <U>  SimpleReactStream<U> construct(Stream s, ExecutorService e, RetryExecutor r, boolean eager);

	
	
	
	/**
	 * Start a reactive dataflow from a stream of CompletableFutures.
	 * 
	 * @param stream of CompletableFutures that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public <U> SimpleReactStream<U> fromStream(final Stream<CompletableFuture<U>> stream) {

		Stream s = stream;
		return  construct( s,getExecutor(),getRetrier(), isEager());
	}
	/**
	 * Start a reactive dataflow from a stream.
	 * 
	 * @param stream that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public <U> SimpleReactStream<U> fromStreamWithoutFutures(final Stream<U> stream) {
		
		Stream s = stream.map(it -> CompletableFuture.completedFuture(it));
		return construct( s,this.getExecutor(), getRetrier(),isEager());
	}

	public <U> SimpleReactStream<U> of(U...array){
		return fromStreamWithoutFutures(Stream.of(array));
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
	public <U> SimpleReactStream<U> react(final List<Supplier<U>> actions) {

		return react((Supplier[]) actions.toArray(new Supplier[] {}));
	}
	private final Object iterationLock = "iterationLock";
	
	/**
	 * Start a reactive flow from a JDK Iterator
	 * 
	 * @param iterator SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
	 * @param maxTimes Maximum number of iterations
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <U> SimpleReactStream<U> react(final Iterator<U> iterator, int maxTimes){
		return (SimpleReactStream<U>) this.react(() -> {
			synchronized(iterationLock) {
				if(!iterator.hasNext()) 
					return FutureStreamImpl.MISSING_VALUE;
			return iterator.next();
			}
		},SimpleReact.times(maxTimes)).filter(it->it!=FutureStreamImpl.MISSING_VALUE);
	
	}
	/**
	 * Start a reactive flow from a Collection using an Iterator
	 * 
	 * @param collection - Collection SimpleReact will iterate over at the start of the flow
	 *
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <R> SimpleReactStream<R> reactToCollection(final Collection<R> collection){
		return react(collection.iterator(),collection.size());
	}
	
	
	/**
	 * Start a reactive dataflow from a single Supplier, which will be executed repeatedly according to rules defined by the generator.
	 * 
	 * Example : 
	 * To execute the same Supplier 4 times use :
	 * <code>
	 * List&lt;String&gt; strings = new SimpleReact()
				.&lt;Integer&gt; react(() -&gt; count++ ,SimpleReact.times(4))
	 * </code>
	 * To skip the first 5 iterations and take the next 5
	 *  * <code>
	 * List&lt;String&gt; strings = new SimpleReact()
				.&lt;Integer&gt; react(() -&gt; count++ ,SimpleReact.times(5).offset(5))
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
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <U> SimpleReactStream< U> react(final Supplier<U> s, Generator t) {

		return construct(t.generate(s),
				this.getExecutor(),getRetrier(),isEager());

	}
	/**
	 * Generate an infinite reactive flow. Requires a lazy flow.
	 * 
	 * The flow will run indefinitely unless / until the provided Supplier throws an Exception
	 * 
	 * @see com.aol.simple.react.async.Queue   SimpleReact Queue for a way to create a more managable infinit flow
	 * 
	 * @param s Supplier to generate the infinite flow
	 * @return Next stage in the flow
	 */
	public <U> SimpleReactStream< U> reactInfinitely(final Supplier<U> s) {
		if(isEager())
			throw new InfiniteProcessingException("To reactInfinitely use a lazy stream");
		return construct(Stream.generate(() -> CompletableFuture.completedFuture(s.get())),
				this.getExecutor(),getRetrier(),false);

	}
	
	public <U> SimpleReactStream<U> iterateInfinitely(final U seed, final UnaryOperator<U> f){
		if(isEager())
			throw new InfiniteProcessingException("To iterateInfinitely use a lazy stream");
		return construct(Stream.iterate(seed, it -> f.apply(it)).map(it -> CompletableFuture.completedFuture(it)),
				this.getExecutor(),getRetrier(),false);
	}
	/**
	 * Create a Sequential Generator that will trigger a Supplier to be called the specified number of times
	 * 
	 * @param times Number of times the Supplier should be called at the start of the reactive dataflow
	 * @return Sequential Generator
	 */
	@SuppressWarnings("rawtypes")
	public static ParallelGenerator times(int times){
		return new  ParallelGenerator(times,0);
	
		
	}
	/**
	 * Create a Parallel Generator that will trigger a Supplier to be called the specified number of times
	 * 
	 * @param times Number of times the Supplier should be called at the start of the reactive dataflow
	 * @return Parellel Generator
	 */
	@SuppressWarnings("rawtypes")
	public static ParallelGenerator timesInSequence(int times){
		return new  ParallelGenerator(times,0);
	
		
	}
	/**
	 * Start a reactive dataflow that calls the supplied function iteratively, with each output, feeding into the next input
	 *
	 * Example :-
	 * 
	 * <code>
	 * List&lt;Integer&gt; results = new SimpleReact()
				.&lt;Integer&gt; react((input) -&gt; input + 1,iterate(0).times(1).offset(10))
	 * </code>
	 * 
	 * 
	 * @param f Function to be called iteratively
	 * @param t Iterator that manages function call
	 * @return Next stage in the reactive flow
	 */
	public <U> SimpleReactStream<U> react(final Function<U,U> f,ReactIterator<U> t) {

		Stream s = t.iterate(f);
		return construct(s,this.getExecutor(),getRetrier(),isEager());

	}
	/**
	 * Create an iterator that manages a function call starting with the supplied seed value
	 * 
	 * @param seed Initial value that iterator will apply to the function it iterates over
	 * @return Populated ReactIterator
	 */
	public static <T> ReactIterator<T> iterate(T seed){
		return new  SequentialIterator<T>(seed);
	
		
	}
	
	
	/**
	 * 
	 * Start a reactive dataflow with an array of one-off-suppliers
	 * 
	 * @param actions Array of Suppliers to provide data (and thus events) that
	 *            downstream jobs will react too
	 * @return Next stage in the reactive flow
	 */
	public <U> SimpleReactStream<U> react(final Supplier<U>... actions) {

		return this.<U> reactI(actions);

	}
	
	
	/**
	 * This internal method has been left protected, so it can be mocked / stubbed as some of the entry points are final
	 * 
	 */
	@SuppressWarnings("unchecked")
	@VisibleForTesting
	protected <U> SimpleReactStream<U> reactI(final Supplier<U>... actions) {
		
		
			return construct(Stream.of(actions).map(
				next -> CompletableFuture.supplyAsync(next, this.getExecutor())),
				this.getExecutor(),getRetrier(),isEager());
		
		
	}
	
	
	
	
		
	
}
