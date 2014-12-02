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
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import sun.misc.Unsafe;

public class Stage<T, U> {

	private final Logger logger = Logger.getLogger(this.getClass().getName());
	private final Stream<CompletableFuture<T>> stream;
	private volatile List<CompletableFuture> lastActive;
	private final Executor forkJoinPool;
	private volatile Optional<Consumer<Throwable>> errorHandler = Optional.empty();
	private final  Unsafe unsafe = getUnsafe();
	
	public Stage(Stream<CompletableFuture<T>> stream, Executor forkJoinPool) {

		this.stream = stream;
		this.forkJoinPool = forkJoinPool;
	}

	public List<CompletableFuture<U>> with(Function<T, ? extends Object> fn) {
		
		return stream.map(future -> (CompletableFuture<U>) future.thenApplyAsync(fn, forkJoinPool)).collect(Collectors.toList());
	}
	
	
	public <R> Stage<U, R> then(Function<T, U> fn) {
		if (lastActive != null) {
			lastActive = lastActive.stream().map((ft) -> ft.thenApplyAsync(fn, forkJoinPool)).collect(Collectors.toList());
		} else
			lastActive = stream.map(future -> future.thenApplyAsync(fn, forkJoinPool)).collect(Collectors.toList());
		return (Stage<U, R>)this;
	}

	public<R> Stage<T, R> onFail(Function<? extends Throwable, T> fn) {
		if (lastActive != null) {
			lastActive = lastActive.stream().map((ft) -> ft.exceptionally(fn)).collect(Collectors.toList());
		} else
			lastActive = stream.map(future -> future.exceptionally((Function<Throwable, ? extends T>) fn)).collect(Collectors.toList());
		return (Stage<T, R>)this;
	}

	public Stage<T, U> capture(Consumer<? extends Throwable> error) {
		this.errorHandler = Optional.of((Consumer<Throwable>) error);
		return this;
	}
	public<R> Stage<U, R> allOf(Function<List<T>, U> fn) {
		List<CompletableFuture> completedFutures =lastActive;
		lastActive = Arrays.asList(CompletableFuture.allOf(lastActive.toArray(new CompletableFuture[0])));
		
		lastActive = lastActive.stream().map((ft) -> ft.thenApplyAsync( (results) -> { 
				
					return fn.apply((List<T>) completedFutures.stream().map( next -> getSafe(next)).collect(Collectors.toList()));
				
			}
		, forkJoinPool)).collect(Collectors.toList());
		
		return (Stage<U, R>)this;
	}

	private Object getSafe(CompletableFuture next) {
		try {
			return next.get();
		} catch (InterruptedException | ExecutionException e) {
			Thread.currentThread().interrupt();
			errorHandler.ifPresent((handler) -> handler.accept(e.getCause()));
		}
		return null;
	}

	public <U> List<U> block() {
		return block(lastActive);
	}
	
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

	private void capture(Exception e) {
		errorHandler.ifPresent((handler) -> handler.accept(e.getCause()));
	}
 
	

	
	@SuppressWarnings("unchecked")
	public <U> List<U> block(Predicate<Status> breakout) {
		
		final CompletableFuture<List<U>> promise = new CompletableFuture<>();
		
		SimpleTimer timer = new SimpleTimer();
		AtomicInteger completed = new AtomicInteger();
		AtomicInteger errors = new AtomicInteger();
		
		Queue<U> currentResults = new  ConcurrentLinkedQueue<U>();
		
		
		lastActive.forEach( f -> f.whenComplete(  (result,ex) ->{
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
				promise.complete(new LinkedList(currentResults));
			}
			
		}));
		
		try {
			return promise.get();
		}catch (ExecutionException e) {
			
			unsafe.throwException(e);
		}
		catch (InterruptedException e) {
			
			Thread.currentThread().interrupt();
			unsafe.throwException(e);
		}
		throw new RuntimeException("No result");
	}

	private static Unsafe getUnsafe ()

	{

	    try {

	        Field field = Unsafe.class.getDeclaredField("theUnsafe");

	        field.setAccessible(true);

	        return (Unsafe)field.get(null);

	    } catch (Exception ex) {

	        throw new RuntimeException("can't get Unsafe instance", ex);

	    }

	}
	

	private boolean allResultsReturned(int localComplete) {
		return localComplete==lastActive.size();
	}
	@SuppressWarnings("unchecked")
	private boolean breakoutConditionsMet(Predicate<Status> breakout, Status  status) {
		return breakout.test(status);
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
