package com.oath.cyclops.internal.stream;

import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.oath.cyclops.internal.stream.spliterators.ClosingSpliterator;
import com.oath.cyclops.types.stream.Connectable;
import cyclops.reactive.ReactiveSeq;
import cyclops.companion.Streams;

public abstract class BaseConnectableImpl<T> extends IteratorHotStream<T>implements Connectable<T> {

    protected final Stream<T> stream;

    public BaseConnectableImpl(final Stream<T> stream) {
        this.stream = stream;
    }

    public Connectable<T> paused(final Executor exec) {
        pause();
        return init(exec);
    }

    public abstract Connectable<T> init(Executor exec);

    public Connectable<T> schedule(final String cron, final ScheduledExecutorService ex) {
        final Iterator<T> it = stream.iterator();
        scheduleInternal(it, cron, ex);
        return this;

    }

    public Connectable<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        final Iterator<T> it = stream.iterator();
        scheduleFixedDelayInternal(it, delay, ex);
        return this;

    }

    public Connectable<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        final Iterator<T> it = stream.iterator();
        scheduleFixedRate(it, rate, ex);
        return this;

    }

    @Override
    public ReactiveSeq<T> connect(final Queue<T> queue) {
        connections.getAndSet(connected, queue);
        connected++;
        unpause();
        return Streams.reactiveSeq(StreamSupport.stream(new ClosingSpliterator(
                                                                                   Long.MAX_VALUE, queue, open),
                                                            false),
                                       Optional.empty());
    }

}
