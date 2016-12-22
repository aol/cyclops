package com.aol.cyclops.internal.stream.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.function.LongConsumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ReversingRangeLongSpliterator implements Spliterator.OfLong, ReversableSpliterator<Long> {

    private long index;
    private final long min;
    private final long max;
    @Getter
    @Setter
    private boolean reverse;

    public ReversingRangeLongSpliterator(final long min, final long max, final boolean reverse) {
        this.min = Math.min(min, max) - 1;
        this.max = Math.max(min, max);
        this.reverse = this.max >= this.min ? reverse : !reverse;
        index = Math.min(min, max);
    }

    @Override
    public ReversableSpliterator invert() {
        setReverse(!isReverse());
        index = max - 1;
        return this;
    }

    @Override
    public boolean tryAdvance(final LongConsumer consumer) {
        Objects.requireNonNull(consumer);
        if (!reverse) {
            if (index < max && index > min) {
                consumer.accept(index++);
                return true;
            }
        }
        if (reverse) {
            if (index > min && index < max) {
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
    public Spliterator.OfLong trySplit() {
        return this;
    }

    @Override
    public ReversableSpliterator copy() {
        return new ReversingRangeLongSpliterator(
                                                 index, max, reverse);
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator.OfInt#forEachRemaining(java.util.function.IntConsumer)
     */
    @Override
    public void forEachRemaining(LongConsumer action) {
        long index = this.index; //use local index making spliterator reusable
        if (!reverse) {
            for( ;index < max && index > min;) {
                action.accept(index++);

            }
        }
        if (reverse) {
            for( ;index > min && index < max;) {
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
            for( ;index < max && index > min;) {
                action.accept(index++);
            }
        }
        if (reverse) {
            for( ;index > min && index < max;) {
                action.accept(index--);
            }

        }
    }

}