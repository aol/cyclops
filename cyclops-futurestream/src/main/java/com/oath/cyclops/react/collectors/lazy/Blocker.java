package com.oath.cyclops.react.collectors.lazy;

import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import cyclops.data.ImmutableList;
import cyclops.data.Seq;
import com.oath.cyclops.react.Status;
import com.oath.cyclops.util.SimpleTimer;
import com.oath.cyclops.util.ThrowsSoftened;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class Blocker<U> {

    @SuppressWarnings("rawtypes")
    private final List<CompletableFuture<?>> lastActive;
    private final Optional<Consumer<Throwable>> errorHandler;
    private final CompletableFuture<ImmutableList<U>> promise = new CompletableFuture<>();

    private final SimpleTimer timer = new SimpleTimer();
    private final AtomicInteger completed = new AtomicInteger();
    private final AtomicInteger errors = new AtomicInteger();

    private final Queue<U> currentResults = new ConcurrentLinkedQueue<U>();

    @SuppressWarnings("unchecked")
    @ThrowsSoftened({ InterruptedException.class, ExecutionException.class })
    public ImmutableList<U> block(final Predicate<Status<U>> breakout) {


        return nonBlocking(breakout).join();

    }
    public CompletableFuture<ImmutableList<U>> nonBlocking(final Predicate<Status<U>> breakout) {

        if (lastActive.size() == 0)
            return CompletableFuture.completedFuture(Seq.empty());
        lastActive.forEach(f -> f.whenComplete((result, ex) -> {
            testBreakoutConditionsBeforeUnblockingCurrentThread(breakout, result, (Throwable) ex);
        }));

        return promise;

    }

    private Status buildStatus(final Throwable ex) {
        if (ex != null) {
            errors.incrementAndGet();

        } else {
            completed.incrementAndGet();
        }

        return new Status(
                          completed.get(), errors.get(), lastActive.size(), timer.getElapsedNanoseconds(), Seq.fromIterable(currentResults));

    }

    private void testBreakoutConditionsBeforeUnblockingCurrentThread(final Predicate<Status<U>> breakout, final Object result, final Throwable ex) {

        if (result != null)
            currentResults.add((U) result);

        final Status status = buildStatus(ex); //new results may be added after status object is created
        if (ex != null) {
            errorHandler.ifPresent((handler) -> handler.accept(((Exception) ex).getCause()));
        }

        if (breakoutConditionsMet(breakout, status) || allResultsReturned(status.getCompleted() + status.getErrors())) {
            promise.complete(Seq.fromIterable(currentResults));
        }
    }

    private boolean allResultsReturned(final int localComplete) {
        return localComplete == lastActive.size();
    }

    private boolean breakoutConditionsMet(final Predicate<Status<U>> breakout, final Status status) {
        return breakout.test(status);
    }

}
