package com.aol.simple.react;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sun.misc.Unsafe;

public class Stage<T, U> {

	private final static Logger logger = Logger.getLogger(Stage.class.getName());
	private final Stream<CompletableFuture<T>> stream;
	private final Executor forkJoinPool;
	
	@SuppressWarnings("rawtypes")
	private volatile List<CompletableFuture> lastActive;
	private volatile Optional<Consumer<Throwable>> errorHandler = Optional.empty();
	
	
	/**
	 * 
	 * Construct a SimpleReact stage - this acts as a fluent SimpleReact builder
	 * 
	 * @param stream  Stream that will generate the events that will be reacted to.
	 * @param executor The next stage's tasks will be submitted to this executor
	 */
	public Stage(Stream<CompletableFuture<T>> stream, Executor executor) {
		this.stream = stream;
		this.forkJoinPool = executor;
	}

	/**
	 * @param fn
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<CompletableFuture<U>> with(Function<T, ? extends Object> fn) {
		
		return stream.map(future -> (CompletableFuture<U>) future.thenApplyAsync(fn, forkJoinPool)).collect(Collectors.toList());
	}
	
	@SuppressWarnings("unchecked")
	public <R> Stage<U, R> then(Function<T, U> fn) {
		if (lastActive != null) {
			lastActive = lastActive.stream().map((ft) -> ft.thenApplyAsync(fn, forkJoinPool)).collect(Collectors.toList());
		} else
			lastActive = stream.map(future -> future.thenApplyAsync(fn, forkJoinPool)).collect(Collectors.toList());
		return (Stage<U, R>)this;
	}

	@SuppressWarnings("unchecked")
	public<R> Stage<T, R> onFail(Function<? extends Throwable, T> fn) {
		if (lastActive != null) {
			lastActive = lastActive.stream().map((ft) -> ft.exceptionally(fn)).collect(Collectors.toList());
		} else
			lastActive = stream.map(future -> future.exceptionally((Function<Throwable, ? extends T>) fn)).collect(Collectors.toList());
		return (Stage<T, R>)this;
	}

	@SuppressWarnings("unchecked")
	public Stage<T, U> capture(Consumer<? extends Throwable> error) {
		this.errorHandler = Optional.of((Consumer<Throwable>) error);
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public<R> Stage<U, R> allOf(Function<List<T>, U> fn) {
		
		@SuppressWarnings("rawtypes")
		List<CompletableFuture> completedFutures =lastActive;
		lastActive = Arrays.asList(CompletableFuture.allOf(lastActive.toArray(new CompletableFuture[0])));
		
		lastActive = lastActive.stream().map((ft) -> ft.thenApplyAsync( (results) -> { 
				
					return fn.apply((List<T>) completedFutures.stream().map( next -> getSafe(next)).collect(Collectors.toList()));
				
			}
		, forkJoinPool)).collect(Collectors.toList());
		
		return (Stage<U, R>)this;
	}

	

	@SuppressWarnings("hiding")
	public <U> List<U> block() {
		return block(lastActive);
	}
	
	
	
	
	@SuppressWarnings("hiding")
	public <U> List<U> block(final Predicate<Status> breakout) {
		return new Blocker<U>(lastActive,errorHandler).block(breakout);		
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked", "hiding" })
	private <U> List<U> block(List<CompletableFuture> lastActive) {
		return lastActive.stream().map((future) -> {

			try {
				return (U) future.get();
			}catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				capture(e);
				return null;
			}
			catch (Exception e) {
				
				capture(e);
				return null;
			}

		}).filter(v -> v != null).collect(Collectors.toList());
	}
	
	private void capture(final Exception e) {
		errorHandler.ifPresent((handler) -> handler.accept(e.getCause()));
	}
	
	@SuppressWarnings("rawtypes")
	private Object getSafe( CompletableFuture next) {
		try {
			return next.get();
		} catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			errorHandler.ifPresent((handler) -> handler.accept(e.getCause()));
		}
		return null;
	}
	
	

	
	@AllArgsConstructor
	private static class Blocker<U> {
		
		
		private final  Optional<Unsafe> unsafe = getUnsafe();
		@SuppressWarnings("rawtypes")
		private final List<CompletableFuture> lastActive;
		private final Optional<Consumer<Throwable>> errorHandler;
		private final CompletableFuture<List<U>> promise = new CompletableFuture<>();
		
		private final SimpleTimer timer = new SimpleTimer();
		private final AtomicInteger completed = new AtomicInteger();
		private final AtomicInteger errors = new AtomicInteger();
		
		private final Queue<U> currentResults = new  ConcurrentLinkedQueue<U>();
		
		@SuppressWarnings("unchecked")
		public  List<U> block( final Predicate<Status> breakout) {
			
			lastActive.forEach( f -> f.whenComplete(  (result,ex) ->{
				testBreakoutConditionsBeforeUnblockingCurrentThread(breakout, result, ex);
				
			}));
			
			try {
				return promise.get();
			}catch (ExecutionException e) {
				unsafe.ifPresent( u -> u.throwException(e));
				throw new RuntimeException(e);
			}
			catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				unsafe.ifPresent( u -> u.throwException(e));
				throw new RuntimeException(e);
			}
			
		}

		private void testBreakoutConditionsBeforeUnblockingCurrentThread(final Predicate<Status> breakout,
				Object result, Object ex) {
			if(result!=null)
				currentResults.add((U)result);
			int localComplete = completed.incrementAndGet();
			int localErrors = errors.get();
			if(ex!=null){
				localErrors = errors.incrementAndGet();
				errorHandler.ifPresent((handler) -> handler.accept( ((Exception)ex).getCause() ));
			}
			Status status = new Status(localComplete,localErrors,lastActive.size(),timer.getElapsedMilliseconds());
			if(breakoutConditionsMet(breakout,status) || allResultsReturned(localComplete)){
				promise.complete(new LinkedList<U>(currentResults));
			}
		}
		
		private boolean allResultsReturned(int localComplete) {
			return localComplete==lastActive.size();
		}
		
		private boolean breakoutConditionsMet(Predicate<Status> breakout, Status  status) {
			return breakout.test(status);
		}
		
		private static Optional<Unsafe> getUnsafe (){

		    try {

		        Field field = Unsafe.class.getDeclaredField("theUnsafe");

		        field.setAccessible(true);

		        return Optional.of((Unsafe)field.get(null));

		    } catch (Exception ex) {

		       logger.log(Level.SEVERE, ex.getMessage());

		    }
		    return Optional.empty();

		}
	}
	
	@AllArgsConstructor
	@Getter
	public static class Status {
		private final int completed;
		private final int errors;
		private final int total;
		private final long elapsedMillis;
	}

}
