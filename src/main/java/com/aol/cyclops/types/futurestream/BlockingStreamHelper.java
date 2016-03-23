package com.aol.cyclops.types.futurestream;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.internal.react.async.future.FastFuture;
import com.aol.cyclops.internal.react.exceptions.FilteredExecutionPathException;
import com.aol.cyclops.internal.react.exceptions.SimpleReactCompletionException;
import com.aol.cyclops.internal.react.stream.EagerStreamWrapper;
import com.aol.cyclops.internal.react.stream.LazyStreamWrapper;
import com.aol.cyclops.internal.react.stream.MissingValue;
import com.aol.cyclops.react.SimpleReactFailedStageException;
import com.aol.cyclops.util.ExceptionSoftener;

public class BlockingStreamHelper {
	
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R block(BlockingStream blocking,final Collector collector, final EagerStreamWrapper lastActive) {
		Stream<CompletableFuture> stream = lastActive.stream();
		
		return (R) stream.map((future) -> {
			return  BlockingStreamHelper.getSafe(future,blocking.getErrorHandler());
		}).filter(v -> v != MissingValue.MISSING_VALUE).collect(collector);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R block(BlockingStream blocking,final Collector collector, final LazyStreamWrapper lastActive) {
		
		
		return (R)((LazyStream)blocking).run(collector);
		
			
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R aggregateResults(final Collector collector,
			final List<FastFuture> completedFutures, Optional<Consumer<Throwable>> errorHandler) {
		return (R) completedFutures.stream().map(next -> getSafe(next,errorHandler))
				.filter(v -> v != MissingValue.MISSING_VALUE).collect(collector);
	}
	@SuppressWarnings({ "rawtypes", "unchecked" })
	static <R> R aggregateResultsCompletable(final Collector collector,
			final List<CompletableFuture> completedFutures, Optional<Consumer<Throwable>> errorHandler) {
		return (R) completedFutures.stream().map(next -> getSafe(next,errorHandler))
				.filter(v -> v != MissingValue.MISSING_VALUE).collect(collector);
	}
	public static void captureUnwrap(CompletionException e,Optional<Consumer<Throwable>> errorHandler){
	    capture(e.getCause(),errorHandler);
	}
	static void capture(final Throwable t,Optional<Consumer<Throwable>> errorHandler) {
	    SimpleReactFailedStageException.matchable(t)
	                                   .visit(general-> captureGeneral(general,errorHandler),
	                                           sr-> captureFailedStage(sr,errorHandler));
	}
	static Void captureFailedStage(final SimpleReactFailedStageException e,Optional<Consumer<Throwable>> errorHandler) {
		errorHandler.ifPresent((handler) -> {
		   
			if (!(e.getCause() instanceof FilteredExecutionPathException)) {
				handler.accept(e.getCause());
			}
		});
		return null;
	}
	static Void captureGeneral(final Throwable t,Optional<Consumer<Throwable>> errorHandler) {
        errorHandler.ifPresent((handler) ->  handler.accept(t.getCause()));
        return null;
    }
	@SuppressWarnings("rawtypes")
	public static Object getSafe(final FastFuture next,Optional<Consumer<Throwable>> errorHandler) {
		try {
			return next.join();
		}catch (SimpleReactCompletionException e) {
            capture(e.getCause(),errorHandler);
        } 
		catch (RuntimeException e) {
			capture(e,errorHandler);
		} catch (Exception e) {
			capture(e,errorHandler);
		}

		return MissingValue.MISSING_VALUE;
	}
	@SuppressWarnings("rawtypes")
	static Object getSafe(final CompletableFuture next,Optional<Consumer<Throwable>> errorHandler) {
		try {
			return next.get();
		}catch(ExecutionException e){
		    capture(e.getCause(),errorHandler);
		}catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			capture(e,errorHandler);
			throw ExceptionSoftener.throwSoftenedException(e);
		} catch (RuntimeException e) {
			capture(e,errorHandler);
		} catch (Exception e) {
			capture(e,errorHandler);
		}

		return MissingValue.MISSING_VALUE;
	}

}
