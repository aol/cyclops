package com.aol.simple.react.stream.lazy;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import lombok.Getter;

import com.aol.simple.react.async.future.FastFuture;
import com.aol.simple.react.stream.ReactBuilder;
import com.aol.simple.react.stream.traits.LazyFutureStream;
import com.nurkiewicz.asyncretry.RetryExecutor;


public abstract class BaseSimpleReact implements ReactBuilder {

	@Getter
	private final Executor queueService;
	protected abstract Executor getExecutor();

	protected abstract  RetryExecutor getRetrier();
	
	protected abstract boolean isAsync() ;

	
	public abstract <U>  LazyFutureStream<U> construct(Stream s, 
			List<FastFuture> org);
	public abstract <U>  LazyFutureStream<U> constructFutures(Stream<CompletableFuture<U>> s, 
			List<FastFuture> org);

	
	protected BaseSimpleReact(){
		queueService=null;
	}
	protected BaseSimpleReact(Executor queueService){
		this.queueService=queueService;
	}
	
	
	public LazyFutureStream<Integer> range(int startInclusive, int endExclusive){
		return from(IntStream.range(startInclusive, endExclusive));
	}
	/**
	 * Start a reactive flow from a JDK Iterator
	 * 
	 * @param iterator SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <U> LazyFutureStream<U> from(final Iterator<U> iterator){
		return from(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED),false));
		
	
	}
	/**
	 * Start a reactive flow from a Collection using an Iterator
	 * 
	 * @param collection - Collection SimpleReact will iterate over at the start of the flow
	 *
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <R> LazyFutureStream<R> from(final Collection<R> collection){
		return from(collection.stream());
	}


	/**
	 * Start a reactive flow from a JDK Iterator
	 * 
	 * @param iter SimpleReact will iterate over this iterator concurrently to start the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public <U> LazyFutureStream<U> fromIterable(final Iterable<U> iter){
		return this.from(StreamSupport.stream(Spliterators.spliteratorUnknownSize(iter.iterator(), Spliterator.ORDERED),false));
	
	}

	
	/**
	 * Start a reactive dataflow from a stream of CompletableFutures.
	 * 
	 * @param stream of CompletableFutures that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public <U> LazyFutureStream<U> fromStream(final Stream<CompletableFuture<U>> stream) {

		Stream s = stream;
		return  construct( s,null);
	}
	
	/**
	 * Start a reactive dataflow from a stream.
	 * 
	 * @param stream that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public <U> LazyFutureStream<U> from(final Stream<U> stream) {
		
		//Stream s = stream.map(it -> FastFuture.completedFuture(it));
		return construct( stream,null);
	}
	/**
	 * Start a reactive dataflow from a stream.
	 * 
	 * @param stream that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public <U> LazyFutureStream<U> from(final IntStream stream) {
		
		return (LazyFutureStream<U>)from(stream.boxed());
	
	}
	/**
	 * Start a reactive dataflow from a stream.
	 * 
	 * @param stream that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public <U> LazyFutureStream<U> from(final DoubleStream stream) {
		
		return (LazyFutureStream<U>)from(stream.boxed());
	
	}
	/**
	 * Start a reactive dataflow from a stream.
	 * 
	 * @param stream that will be used to drive the reactive dataflow
	 * @return Next stage in the reactive flow
	 */
	public <U> LazyFutureStream<U> from(final LongStream stream) {
		
		return (LazyFutureStream<U>)from(stream.boxed());
	
	}
	


	public <U> LazyFutureStream<U> of(U...array){
		return from(Stream.of(array));
	}
	public <U> LazyFutureStream<U> from(CompletableFuture<U> cf){
		return this.construct(Stream.of(FastFuture.fromCompletableFuture(cf)), Arrays.asList(FastFuture.fromCompletableFuture(cf)));
	}
	public <U> LazyFutureStream<U> from(CompletableFuture<U>... cf){
		return (LazyFutureStream)this.construct(Stream.of(cf).map(FastFuture::fromCompletableFuture), 
						(List)Stream.of(cf).map(FastFuture::fromCompletableFuture).collect(Collectors.toList()));
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
	public <U> LazyFutureStream<U> react(final Collection<Supplier<U>> actions) {

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
	public <U> LazyFutureStream<U> react(final Stream<Supplier<U>> actions) {

		return construct(actions.map(
				next -> CompletableFuture.supplyAsync(next, getExecutor())),
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
	public <U> LazyFutureStream<U> react(final Iterator<Supplier<U>> actions) {

		return construct(StreamSupport.stream(Spliterators.spliteratorUnknownSize(actions, Spliterator.ORDERED),false).map(
				next -> CompletableFuture.supplyAsync(next, getExecutor())),null);
		
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
	public <U> LazyFutureStream<U> reactIterable(final Iterable<Supplier<U>> actions) {

		return construct(StreamSupport.stream(Spliterators.spliteratorUnknownSize(actions.iterator(), Spliterator.ORDERED),false).map(
				next -> CompletableFuture.supplyAsync(next, getExecutor())),
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
	public <U> LazyFutureStream<U> react(final Supplier<U>... actions) {

		return this.<U> reactI(actions);

	}
	
	
	/**
	 * This internal method has been left protected, so it can be mocked / stubbed as some of the entry points are final
	 * 
	 */
	@SuppressWarnings("unchecked")
	protected <U> LazyFutureStream<U> reactI(final Supplier<U>... actions) {
		
		
			return constructFutures(Stream.of(actions).map(
				next -> CompletableFuture.supplyAsync(next, this.getExecutor())),null);
		
		
	}
	
	
	
	
		
	
}
