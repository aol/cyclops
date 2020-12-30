package com.oath.cyclops.internal.stream;

import com.oath.cyclops.util.ExceptionSoftener;
import com.oath.cyclops.util.stream.scheduling.cron.CronExpression;
import cyclops.companion.Eithers;
import cyclops.function.FluentFunctions;

import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class IteratorHotStream<T> {

    protected final AtomicReferenceArray<Queue<T>> connections = new AtomicReferenceArray<>(
                                                                                            10);
    protected final AtomicBoolean open = new AtomicBoolean(
                                                           true);
    protected volatile int connected = 0;
    protected final AtomicReference<CompletableFuture<Void>> pause = new AtomicReference<>(
                                                                                           CompletableFuture.<Void> completedFuture(null));

    public boolean isPaused() {
        return pause.get()
                    .isDone();
    }

    protected void unpause() {
        final CompletableFuture<Void> current = pause.get();
        if (!current.isDone())
            current.complete(null);
    }

    protected void pause() {
        pause.set(new CompletableFuture<Void>());
    }

    protected void scheduleInternal(final Iterator<T> it, final String cron, final ScheduledExecutorService ex) {
        final Date now = new Date();
        final Date d = ExceptionSoftener.softenSupplier(() -> new CronExpression(
                                                                                 cron))
                                        .get()
                                        .getNextValidTimeAfter(now);

        final long delay = d.getTime() - now.getTime();

        ex.schedule(() -> {
            synchronized (it) {
                if (it.hasNext()) {
                    try {
                        final T next = it.next();
                        final int local = connected;

                        for (int i = 0; i < local; i++) {

                            Eithers.blocking(connections.get(i))
                                      .fold(FluentFunctions.ofChecked(in -> {
                                in.put(next);
                                return true;
                            }), q -> q.offer(next));
                        }

                    } finally {

                        scheduleInternal(it, cron, ex);

                    }
                } else {
                    open.set(false);
                }
            }
        } , delay, TimeUnit.MILLISECONDS);

    }

    protected IteratorHotStream<T> scheduleFixedDelayInternal(final Iterator<T> it, final long delay, final ScheduledExecutorService ex) {
        ex.scheduleWithFixedDelay(() -> {
            synchronized (it) {
                if (it.hasNext()) {

                    final T next = it.next();

                    final int local = connected;

                    for (int i = 0; i < local; i++) {

                        Eithers.blocking(connections.get(i))
                                  .fold(FluentFunctions.ofChecked(in -> {
                            in.put(next);
                            return true;
                        }), q -> q.offer(next));

                    }

                } else {
                    open.set(false);
                }
            }
        } , delay, delay, TimeUnit.MILLISECONDS);
        return this;

    }

    protected IteratorHotStream<T> scheduleFixedRate(final Iterator<T> it, final long rate, final ScheduledExecutorService ex) {
        ex.scheduleAtFixedRate(() -> {
            synchronized (it) {
                if (it.hasNext()) {

                    final T next = it.next();

                    final int local = connected;

                    for (int i = 0; i < local; i++) {

                        Eithers.blocking(connections.get(i))
                                  .fold(FluentFunctions.ofChecked(in -> {
                            in.put(next);
                            return true;
                        }), q -> q.offer(next));
                    }

                } else {
                    open.set(false);
                }
            }
        } , 0, rate, TimeUnit.MILLISECONDS);
        return this;

    }
}
