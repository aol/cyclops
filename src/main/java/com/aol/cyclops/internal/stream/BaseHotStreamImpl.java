package com.aol.cyclops.internal.stream;

import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.StreamUtils;
import com.aol.cyclops.internal.stream.spliterators.ClosingSpliterator;
import com.aol.cyclops.types.stream.HotStream;

public abstract class BaseHotStreamImpl<T> extends IteratorHotStream<T>implements HotStream<T> {

    protected final Stream<T> stream;

    public BaseHotStreamImpl(final Stream<T> stream) {
        this.stream = stream;
    }

    public HotStream<T> paused(final Executor exec) {
        pause();
        return init(exec);
    }

    public abstract HotStream<T> init(Executor exec);

    public HotStream<T> schedule(final String cron, final ScheduledExecutorService ex) {
        final Iterator<T> it = stream.iterator();
        scheduleInternal(it, cron, ex);
        return this;

    }

    public HotStream<T> scheduleFixedDelay(final long delay, final ScheduledExecutorService ex) {
        final Iterator<T> it = stream.iterator();
        scheduleFixedDelayInternal(it, delay, ex);
        return this;

    }

    public HotStream<T> scheduleFixedRate(final long rate, final ScheduledExecutorService ex) {
        final Iterator<T> it = stream.iterator();
        scheduleFixedRate(it, rate, ex);
        return this;

    }

    @Override
    public ReactiveSeq<T> connect(final Queue<T> queue) {
        connections.getAndSet(connected, queue);
        connected++;
        unpause();
        return StreamUtils.reactiveSeq(StreamSupport.stream(new ClosingSpliterator(
                                                                                   Long.MAX_VALUE, queue, open),
                                                            false),
                                       Optional.empty(), Optional.empty());
    }

}
