package com.oath.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class LimitWhileTimeSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T> {
    private final Spliterator<T> source;
    private final long time;
    private final TimeUnit t;
    final long toRun;

    boolean closed = false;
    public LimitWhileTimeSpliterator(final Spliterator<T> source, long time, TimeUnit t) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.time = time;
        this.t=t;

        toRun = t.toNanos(time);

    }
    long start =-1;
    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        start = System.nanoTime();
        while(!closed){
            boolean canAdvance = source.tryAdvance(t -> {
                closed = System.nanoTime()-start >= toRun;
                if(!closed)
                    action.accept(t);
            });
            if(!canAdvance){
                closed = true;
                return;
            }

        }


    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(closed)
            return true;
        if(start == -1) {
            start = System.nanoTime();
        }
        boolean canAdvance = source.tryAdvance(t -> {
                closed = System.nanoTime()-start >= toRun;
                if(!closed)
                    action.accept(t);
            });

        if(closed)
            return false;
        return canAdvance;
    }

    @Override
    public Spliterator<T> copy() {
        return new LimitWhileTimeSpliterator<>(CopyableSpliterator.copy(source),time,t);
    }
}
