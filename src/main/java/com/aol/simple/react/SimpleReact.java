package com.aol.simple.react;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;

/**
 * Entry point for creating a concurrent dataflow.
 * 
 * 
 * @author johnmcclean
 *
 * @param <T>
 *            Return type of object created by initial Suppliers
 */
public class SimpleReact<T> {

	@Getter
	static private final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime
			.getRuntime().availableProcessors());

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
	public static <T, U> Stage<T, U> react(final List<Supplier<T>> actions) {

		return react((Supplier[]) actions.toArray(new Supplier[] {}));
	}

	/**
	 * Start a reactive dataflow with a list of one-off-suppliers and an Executor
	 * 
	 * 
	 * @param executor All downstream concurrent tasks will be submitted to this executor
	 * @param actions List of Suppliers to provide data (and thus events) that
	 *            downstream jobs will react too
	 *            
	 * @return Next stage in the reactive flow
	 */
	@SuppressWarnings("unchecked")
	public static <T, U> Stage<T, U> react(final Executor executor,
			final List<Supplier<T>> actions) {

		return react(executor, (Supplier[]) actions.toArray(new Supplier[] {}));
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
	public static <T, U> Stage<T, U> react(final Supplier<T>... actions) {

		return new SimpleReact<T>().<U> reactI(actions);

	}
	
	/**
	 * Start a reactive dataflow with an array of one-off-suppliers and an Executor
	 * 
	 * 
	 * @param executor All downstream concurrent tasks will be submitted to this executor
	 * @param actions Array of Suppliers to provide data (and thus events) that
	 *            downstream jobs will react too
	 *            
	 * @return Next stage in the reactive flow
	 */
	@SafeVarargs
	public static <T, U> Stage<T, U> react(final Executor executor,
			final Supplier<T>... actions) {
		return new SimpleReact<T>().<U> reactI(executor, actions);

	}

	private <U> Stage<T, U> reactI(final Supplier<T>... actions) {
		return reactI(forkJoinPool, actions);
	}

	private <U> Stage<T, U> reactI(final Executor executor,
			final Supplier<T>... actions) {
		return new Stage(Stream.of(actions).map(
				next -> CompletableFuture.supplyAsync(next, executor)),
				executor);
	}

	
	
}
