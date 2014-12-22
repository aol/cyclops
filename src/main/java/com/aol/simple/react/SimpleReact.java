package com.aol.simple.react;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

import lombok.Getter;

import com.aol.simple.react.generators.Generator;
import com.aol.simple.react.generators.ReactIterator;
import com.aol.simple.react.generators.SequentialGenerator;
import com.aol.simple.react.generators.SequentialIterator;

/**
 * Entry point for creating a concurrent dataflow.
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            Return type of object created by initial Suppliers
 */
public class SimpleReact {

	@Getter
	private final ExecutorService executor;

	/**
	 * Construct a new SimpleReact that will use a ForkJoinPool with parrellism set to the number of processors on the host
	 */
	public SimpleReact(){
		this.executor = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
	}
	
	/**
	 * @param executor Executor this SimpleReact instance will use to execute concurrent tasks.
	 */
	public SimpleReact(ExecutorService executor) {
	
		this.executor = executor;
	}
	
	public <U> Stage<U> fromStream(final Stream<CompletableFuture<U>> stream) {

		return  new Stage<U>(stream,executor);
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
	public <U> Stage<U> react(final List<Supplier<U>> actions) {

		return react((Supplier[]) actions.toArray(new Supplier[] {}));
	}
	private final Object iterationLock = "iterationLock";
	
	@SuppressWarnings("unchecked")
	public <U> Stage<U> react(final Iterator<U> iterator, int maxTimes){
		return (Stage<U>) this.<Optional<U>>react(() -> {
			synchronized(iterationLock) {
				if(!iterator.hasNext()) 
					return Optional.empty();
			return Optional.of(iterator.next());
			}
		},SimpleReact.times(maxTimes))
		.<U>filter(it -> it.isPresent())
		.<U>then(it -> it.get());
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <U> Stage< U> react(final Supplier<U> s, Generator t) {

		return new Stage<U>(t.generate(s),
				executor);

	}
	@SuppressWarnings("rawtypes")
	public static Generator times(int times){
		return new  SequentialGenerator(times,0);
	
		
	}
	public <U> Stage<U> react(final Function<U,U> f,ReactIterator<U> t) {

		return new Stage<U>(t.iterate(f),
				executor);

	}
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
	@SafeVarargs
	public final <U> Stage<U> react(final Supplier<U>... actions) {

		return this.<U> reactI(actions);

	}
	
	
	/**
	 * This internal method has been left protected, so it can be mocked / stubbed as some of the entry points are final
	 * 
	 */
	@SuppressWarnings("unchecked")
	@VisibleForTesting
	protected <U> Stage<U> reactI(final Supplier<U>... actions) {
		
		return new Stage<U>(Stream.of(actions).map(
				next -> CompletableFuture.supplyAsync(next, executor)),
				executor);
	}

		
	
}
