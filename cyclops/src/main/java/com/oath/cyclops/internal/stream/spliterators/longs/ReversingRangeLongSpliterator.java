package com.oath.cyclops.internal.stream.spliterators.longs;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import com.oath.cyclops.internal.stream.spliterators.ReversableSpliterator;
import lombok.Getter;
import lombok.Setter;

public class ReversingRangeLongSpliterator implements Spliterator.OfLong,ReversableSpliterator<Long> {

    private final long min;
    private final long max;
    private long index;
    private long start;
    private long step;


    @Getter
    @Setter
    private boolean reverse;

    public ReversingRangeLongSpliterator(final long min, final long max, final long step, final boolean reverse) {
        this.min = min;
        this.max = max;
        this.reverse = reverse;
        this.step =step;
        if(!reverse) {
            start = index = min;
        }
        else {
            start = index = max - 1;
        }
    }
    ReversingRangeLongSpliterator(final long min, final long max, final long step, final boolean reverse,long start) {
        this.min = min;
        this.max = max;
        this.reverse = reverse;
        this.step =step;
        this.start = start;
        if(!reverse) {
            index = start;
        }
        else {
            index = start;
        }

    }

    @Override
    public ReversingRangeLongSpliterator invert() {
        return new ReversingRangeLongSpliterator(min,max,step,!reverse);

    }

    @Override
    public boolean tryAdvance(final LongConsumer consumer) {
        Objects.requireNonNull(consumer);
        if (!reverse) {
            if (index < max && index >= min) {
                consumer.accept(index);
                index = index+step;
                return true;
            }
        }
        if (reverse) {
            if (index >= min && index <= max) {
                consumer.accept(index);
                index = index-step;
                return true;
            }
        }
        return false;
    }

    @Override
    public long estimateSize() {
        return max;
    }

    @Override
    public int characteristics() {
        return IMMUTABLE;
    }

    @Override
    public Spliterator.OfLong trySplit() {
        return this;
    }

    @Override
    public ReversingRangeLongSpliterator copy() {
        return new ReversingRangeLongSpliterator(
            min, max, step,reverse,start);
    }


    @Override
    public void forEachRemaining(LongConsumer action) {
        long index = this.index; //use local index making spliterator reusable
        if (!reverse) {
            for( ;index < max && index >= min;) {
                action.accept(index);
                index = index+step;

            }
        }
        if (reverse) {
            for( ;index >= min && index <= max;) {
                action.accept(index);
                index = index-step;

            }

        }
    }


    @Override
    public void forEachRemaining(Consumer<? super Long> action) {
        long index = this.index; //use local index making spliterator reusable
        if (!reverse) {
            for( ;index < max && index >= min;) {
                action.accept(index);
                index = index+step;
            }
        }
        if (reverse) {
            for( ;index >= min && index <= max;) {
                action.accept(index);
                index = index-step;
            }

        }
    }



}
