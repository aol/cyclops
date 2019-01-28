package com.oath.cyclops.internal.stream.spliterators.ints;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

import com.oath.cyclops.internal.stream.spliterators.ReversableSpliterator;
import lombok.Getter;
import lombok.Setter;

public class ReversingRangeIntSpliterator implements Spliterator.OfInt, ReversableSpliterator<Integer> {

    private final int min;
    private final int max;
    private int index;
     private int start;
    private int step;


    @Getter
    @Setter
    private boolean reverse;

    public ReversingRangeIntSpliterator(final int min, final int max, final int step, final boolean reverse) {
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
    ReversingRangeIntSpliterator(final int min, final int max, final int step, final boolean reverse,int start) {
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
    public ReversableSpliterator invert() {
        return new ReversingRangeIntSpliterator(min,max,step,!reverse);

    }

    @Override
    public boolean tryAdvance(final IntConsumer consumer) {
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
    public Spliterator.OfInt trySplit() {
        return this;
    }

    @Override
    public ReversableSpliterator copy() {
        return new ReversingRangeIntSpliterator(
                min, max, step,reverse,start);
    }


    @Override
    public void forEachRemaining(IntConsumer action) {
        int index = this.index; //use local index making spliterator reusable
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
    public void forEachRemaining(Consumer<? super Integer> action) {
        int index = this.index; //use local index making spliterator reusable
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
