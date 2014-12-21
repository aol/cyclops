package com.aol.simple.react;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Function;
import java.util.function.Predicate;
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
	
	public <T, U> Stage<T, U> fromStream(final Stream<CompletableFuture<T>> stream) {

		return  new Stage<T, U>(stream,executor);
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
	public <T, U> Stage<T, U> react(final List<Supplier<T>> actions) {

		return react((Supplier[]) actions.toArray(new Supplier[] {}));
	}
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public <T, U> Stage<T, U> react(final Supplier<T> s, Generator t) {

		return new Stage<T, U>(t.generate(s),
				executor);

	}
	@SuppressWarnings("rawtypes")
	public static Generator times(int times){
		return new  SequentialGenerator(times,0);
	
		
	}
	public <T, U> Stage<T, U> react(final Function<T,T> f,ReactIterator<T> t) {

		return new Stage<T, U>(t.iterate(f),
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
	public final <T, U> Stage<T, U> react(final Supplier<T>... actions) {

		return this.<T,U> reactI(actions);

	}
	
	
	/**
	 * This internal method has been left protected, so it can be mocked / stubbed as some of the entry points are final
	 * 
	 */
	@SuppressWarnings("unchecked")
	@VisibleForTesting
	protected <T,U> Stage<T, U> reactI(final Supplier<T>... actions) {
		
		return new Stage<T, U>(Stream.of(actions).map(
				next -> CompletableFuture.supplyAsync(next, executor)),
				executor);
	}

		
	
}
