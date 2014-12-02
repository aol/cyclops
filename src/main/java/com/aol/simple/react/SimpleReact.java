package com.aol.simple.react;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class SimpleReact<T> {

	static public final ForkJoinPool forkJoinPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

	@SuppressWarnings("unchecked")
	public static <T, U> Stage<T, U> react(List<Supplier<T>> actions) {

		return react((Supplier[]) actions.toArray(new Supplier[] {}));
	}

	@SuppressWarnings("unchecked")
	public static <T, U> Stage<T, U> react(Executor executor, List<Supplier<T>> actions) {

		return react(executor, (Supplier[]) actions.toArray(new Supplier[] {}));
	}

	
	
	@SafeVarargs
	public static<T,U> Stage<T,U> react(final Supplier<T>... actions){
		
		return new SimpleReact<T>().<U>reactI(actions);
				
	}
	


	
	@SafeVarargs
	public static <T, U> Stage<T, U> react(Executor executor, final Supplier<T>... actions) {
		return new SimpleReact<T>().<U> reactI(executor, actions);

	}

	private <U> Stage<T, U> reactI(final Supplier<T>... actions) {
		return reactI(forkJoinPool, actions);
	}

	private <U> Stage<T, U> reactI(Executor executor, final Supplier<T>... actions) {
		return new Stage(Stream.of(actions).map(next -> CompletableFuture.supplyAsync(next, executor)), executor);
	}

	@SafeVarargs
	public static <T> List<T> block(final Callable<T>... fns) {
		return new SimpleReact<T>().blockI(fns);
	}

	@SafeVarargs
	private final List<T> blockI(final Callable<T>... fns) { //should delegate to Stage block
		return Stream.of(fns).map(fn -> forkJoinPool.submit(fn)).map((future) -> {

			try {
				T t = future.get();
				return t;
			} catch (Exception e) {
				return null;
			}

		}).collect(Collectors.toList());
	}

	public static <T> List<T> sequence(final Callable<T>... fns) {
		return new SimpleReact<T>().sequenceI(fns);
	}

	@SafeVarargs
	private final List<T> sequenceI(final Callable<T>... fns) {
		return Stream.of(fns).map((fn) -> {
			try {
				return fn.call();
			} catch (Exception e) {

			}
			return null;
		}).collect(Collectors.toList());
	}

}
