package com.aol.cyclops2.internal.stream.spliterators.longs;

import com.aol.cyclops2.internal.stream.spliterators.Indexable;
import com.aol.cyclops2.internal.stream.spliterators.ReversableSpliterator;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

public class ReversingRangeLongSpliterator implements Spliterator.OfLong, ReversableSpliterator<Long>, Indexable<Long> {

    private final long min;
    private final long max;
    private long index;
    private long start;

    @Getter
    @Setter
    private boolean reverse;

    public ReversingRangeLongSpliterator(final long min, final long max, final boolean reverse) {
        this.min = min;
        this.max = max;

        this.reverse = reverse;
        if(!reverse)
            start =index = min;
        else
            start = index=max;
    }

    @Override
    public ReversableSpliterator invert() {
        return new ReversingRangeLongSpliterator(min,max,!reverse);

    }

    @Override
    public boolean tryAdvance(final LongConsumer consumer) {
        Objects.requireNonNull(consumer);
        if (!reverse) {
            if (index < max && index >= min) {
                consumer.accept(index++);
                return true;
            }
        }
        if (reverse) {
            if (index > min && index <= max) {
                consumer.accept(index--);
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
    public OfLong trySplit() {
        return this;
    }

    @Override
    public ReversableSpliterator copy() {
        return new ReversingRangeLongSpliterator(
                min, max, reverse);
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator.OfInt#forEachRemaining(java.util.function.IntConsumer)
     */
    @Override
    public void forEachRemaining(LongConsumer action) {
        long index = this.index; //use local index making spliterator reusable
        if (!reverse) {
            for( ;index < max && index >= min;) {
                action.accept(index++);

            }
        }
        if (reverse) {
            for( ;index > min && index <= max;) {
                action.accept(index--);

            }

        }
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator.OfInt#forEachRemaining(java.util.function.Consumer)
     */
    @Override
    public void forEachRemaining(Consumer<? super Long> action) {
        long index = this.index; //use local index making spliterator reusable
        if (!reverse) {
            for( ;index < max && index >= min;) {
                action.accept(index++);
            }
        }
        if (reverse) {
            for( ;index > min && index <= max;) {
                action.accept(index--);
            }

        }
    }

    @Override
    public Spliterator<Long> skip(long offset) {
        if(reverse){
            return new ReversingRangeLongSpliterator(
                    min, max-(int)offset, reverse);

        }else{
            return new ReversingRangeLongSpliterator(
                    start+(int)offset, max, reverse);
        }


    }

    @Override
    public Spliterator<Long> take(long number) {
        if(reverse){
            return new ReversingRangeLongSpliterator(
                    max-(int)number, max, reverse);

        }
        else{
            return new ReversingRangeLongSpliterator(
                    min, start+(int)number, reverse);

        }


    }


}