package com.aol.simple.react;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
class Blocker<U> {

	private final ExceptionSoftener exceptionSoftener = ExceptionSoftener.singleton.factory.getInstance();
	@SuppressWarnings("rawtypes")
	private final List<CompletableFuture> lastActive;
	private final Optional<Consumer<Throwable>> errorHandler;
	private final CompletableFuture<List<U>> promise = new CompletableFuture<>();

	private final SimpleTimer timer = new SimpleTimer();
	private final AtomicInteger completed = new AtomicInteger();
	private final AtomicInteger errors = new AtomicInteger();

	private final Queue<U> currentResults = new ConcurrentLinkedQueue<U>();
	

	@SuppressWarnings("unchecked")
	@ThrowsSoftened({InterruptedException.class,ExecutionException.class})
	public List<U> block(final Predicate<Status> breakout) {

		lastActive.forEach(f -> f.whenComplete((result, ex) -> {
			testBreakoutConditionsBeforeUnblockingCurrentThread(breakout,
					result, (Throwable)ex);
		}));

		try {
			return promise.get();
		} catch (ExecutionException e) {
			exceptionSoftener.throwSoftenedException(e);
			
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			exceptionSoftener.throwSoftenedException(e);
			
		}
		throw new RuntimeException("Unreachable code reached!");
	}

	

	private Status buildStatus(Throwable ex){
		if (ex != null) {
			errors.incrementAndGet();
			
		}else{
			completed.incrementAndGet();
		}
		
		return new Status(completed.get(), errors.get(),
				lastActive.size(), timer.getElapsedNanoseconds());
		
	}
	private void testBreakoutConditionsBeforeUnblockingCurrentThread(
			final Predicate<Status> breakout, final Object result,
			final Throwable ex) {
		if (result != null)
			currentResults.add((U) result); 
		

		final Status status = buildStatus(ex); //new results may be added after status object is created
		if (ex != null) {
			errorHandler.ifPresent((handler) -> handler
					.accept(((Exception) ex).getCause()));
		}
		
		
		if (breakoutConditionsMet(breakout, status)
				|| allResultsReturned(status.getCompleted() + status.getErrors())) {
			promise.complete(new LinkedList<U>(currentResults));
		}
	}

	private boolean allResultsReturned(final int localComplete) {
		return localComplete == lastActive.size();
	}

	private boolean breakoutConditionsMet(final Predicate<Status> breakout,
			final Status status) {
		return breakout.test(status);
	}

	
}
