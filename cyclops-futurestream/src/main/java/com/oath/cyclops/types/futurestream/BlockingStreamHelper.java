package com.oath.cyclops.types.futurestream;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.oath.cyclops.internal.react.async.future.FastFuture;
import com.oath.cyclops.internal.react.exceptions.FilteredExecutionPathException;
import com.oath.cyclops.internal.react.exceptions.SimpleReactCompletionException;
import com.oath.cyclops.internal.react.stream.EagerStreamWrapper;
import com.oath.cyclops.internal.react.stream.LazyStreamWrapper;
import com.oath.cyclops.internal.react.stream.MissingValue;
import com.oath.cyclops.react.SimpleReactFailedStageException;
import com.oath.cyclops.util.ExceptionSoftener;

public class BlockingStreamHelper {

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <T, A, R> R block(final BlockingStream<T> blocking, final Collector collector, final EagerStreamWrapper lastActive) {
        final Stream<CompletableFuture> stream = lastActive.stream();

        return (R) stream.map((future) -> {
            return getSafe(future, blocking.getErrorHandler());
        })
                         .filter(v -> v != MissingValue.MISSING_VALUE)
                         .collect(collector);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <R> R block(final BlockingStream blocking, final Collector collector, final LazyStreamWrapper lastActive) {

        return (R) ((LazyStream) blocking).run(collector);

    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <R> R aggregateResults(final Collector collector, final List<FastFuture> completedFutures,
            final Optional<Consumer<Throwable>> errorHandler) {
        return (R) completedFutures.stream()
                                   .map(next -> getSafe(next, errorHandler))
                                   .filter(v -> v != MissingValue.MISSING_VALUE)
                                   .collect(collector);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    static <R> R aggregateResultsCompletable(final Collector collector, final List<CompletableFuture> completedFutures,
            final Optional<Consumer<Throwable>> errorHandler) {
        return (R) completedFutures.stream()
                                   .map(next -> getSafe(next, errorHandler))
                                   .filter(v -> v != MissingValue.MISSING_VALUE)
                                   .collect(collector);
    }

    public static void captureUnwrap(final Throwable e, final Optional<Consumer<Throwable>> errorHandler) {
        if (e instanceof SimpleReactFailedStageException)
            captureFailedStage((SimpleReactFailedStageException) e, errorHandler);
        else if (e.getCause() != null)
            capture(e.getCause(), errorHandler);
        else
            captureGeneral(e, errorHandler);
    }

    static void capture(final Throwable t, final Optional<Consumer<Throwable>> errorHandler) {
        SimpleReactFailedStageException.matchable(t)
                                       .fold(general -> captureGeneral(general, errorHandler), sr -> captureFailedStage(sr, errorHandler));
    }

    static Void captureFailedStage(final SimpleReactFailedStageException e, final Optional<Consumer<Throwable>> errorHandler) {
        errorHandler.ifPresent((handler) -> {

            if (!(e.getCause() instanceof FilteredExecutionPathException)) {
                handler.accept(e.getCause());
            }
        });
        return null;
    }

    static Void captureGeneral(final Throwable t, final Optional<Consumer<Throwable>> errorHandler) {
        if (t instanceof FilteredExecutionPathException)
            return null;
        errorHandler.ifPresent((handler) -> handler.accept(t));
        return null;
    }
    @SuppressWarnings("rawtypes")
    public static Object throwSafe(final FastFuture next, final Optional<Consumer<Throwable>> errorHandler) {
        try {
            return next.join();
        } catch (final SimpleReactCompletionException e) {
            capture(e.getCause(), errorHandler);
        } catch (final RuntimeException e) {
           throw e;
        } catch (final Exception e) {
            capture(e, errorHandler);
        }

        return MissingValue.MISSING_VALUE;
    }

    @SuppressWarnings("rawtypes")
    public static Object getSafe(final FastFuture next, final Optional<Consumer<Throwable>> errorHandler) {
        try {
            return next.join();
        } catch (final SimpleReactCompletionException e) {
            capture(e.getCause(), errorHandler);
        } catch (final RuntimeException e) {
            capture(e, errorHandler);
        } catch (final Exception e) {
            capture(e, errorHandler);
        }

        return MissingValue.MISSING_VALUE;
    }
    @SuppressWarnings("rawtypes")
    public static Object extractNonFiltered(final FastFuture next, final Optional<Consumer<Throwable>> errorHandler) {

        try {
            return next.join();
        } catch (final SimpleReactCompletionException e) {
            Throwable t = e;

            while(t instanceof SimpleReactCompletionException){
                t= t.getCause();
            }
            while(t instanceof SimpleReactFailedStageException){
                t= t.getCause();
            }
            if(t instanceof FilteredExecutionPathException){
                return MissingValue.MISSING_VALUE;
            }else{
                throw ExceptionSoftener.throwSoftenedException(t);
            }
        } catch (final RuntimeException e) {
            capture(e, errorHandler);
        } catch (final Exception e) {
            capture(e, errorHandler);
        }

        return MissingValue.MISSING_VALUE;
    }

    @SuppressWarnings("rawtypes")
    static Object getSafe(final CompletableFuture next, final Optional<Consumer<Throwable>> errorHandler) {
        try {
            return next.get();
        } catch (final ExecutionException e) {
            capture(e.getCause(), errorHandler);
        } catch (final InterruptedException e) {
            Thread.currentThread()
                  .interrupt();
            capture(e, errorHandler);
            throw ExceptionSoftener.throwSoftenedException(e);
        } catch (final RuntimeException e) {
            capture(e, errorHandler);
        } catch (final Exception e) {
            capture(e, errorHandler);
        }

        return MissingValue.MISSING_VALUE;
    }

}
