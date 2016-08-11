package com.aol.cyclops.internal.stream;

import java.util.Date;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceArray;

import com.aol.cyclops.Matchables;
import com.aol.cyclops.control.FluentFunctions;
import com.aol.cyclops.util.ExceptionSoftener;
import com.aol.cyclops.util.stream.scheduling.cron.CronExpression;

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
        CompletableFuture<Void> current = pause.get();
        if (!current.isDone())
            current.complete(null);
    }

    protected void pause() {
        pause.set(new CompletableFuture<Void>());
    }

    protected void scheduleInternal(Iterator<T> it, String cron, ScheduledExecutorService ex) {
        Date now = new Date();
        Date d = ExceptionSoftener.softenSupplier(() -> new CronExpression(
                                                                           cron))
                                  .get()
                                  .getNextValidTimeAfter(now);

        long delay = d.getTime() - now.getTime();

        ex.schedule(() -> {
            synchronized (it) {
                if (it.hasNext()) {
                    try {
                        T next = it.next();

                        int local = connected;

                        for (int i = 0; i < local; i++) {

                            Matchables.blocking(connections.get(i))
                                      .visit(FluentFunctions.ofChecked(in -> {
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

    protected IteratorHotStream<T> scheduleFixedDelayInternal(Iterator<T> it, long delay, ScheduledExecutorService ex) {
        ex.scheduleWithFixedDelay(() -> {
            synchronized (it) {
                if (it.hasNext()) {

                    T next = it.next();

                    int local = connected;

                    for (int i = 0; i < local; i++) {

                        Matchables.blocking(connections.get(i))
                                  .visit(FluentFunctions.ofChecked(in -> {
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

    protected IteratorHotStream<T> scheduleFixedRate(Iterator<T> it, long rate, ScheduledExecutorService ex) {
        ex.scheduleAtFixedRate(() -> {
            synchronized (it) {
                if (it.hasNext()) {

                    T next = it.next();

                    int local = connected;

                    for (int i = 0; i < local; i++) {

                        Matchables.blocking(connections.get(i))
                                  .visit(FluentFunctions.ofChecked(in -> {
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
