package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
public class ReversingRangeLongSpliterator implements Spliterator.OfLong, ReversableSpliterator<Long>,Indexable<Long> {

    private long index;
    private long start;
    private final long min;
    private final long max;
    @Getter
    @Setter
    private boolean reverse;

    public ReversingRangeLongSpliterator(final long min, final long max, final boolean reverse) {
        this.min = Math.min(min, max) - 1;
        this.max = Math.max(min, max);
        this.reverse = this.max >= this.min ? reverse : !reverse;
        start= index = Math.min(min, max);
    }

    @Override
    public ReversableSpliterator invert() {
        setReverse(!isReverse());
        start=index = max - 1;
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
                                                 start, max, reverse);
    }

    /* (non-Javadoc)
     * @see java.util.Spliterator.OfInt#forEachRemaining(java.util.function.IntConsumer)
     */
    @Override
    public void forEachRemaining(LongConsumer action) {

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

    @Override
    public Spliterator<Long> start(long offset) {
        return new ReversingRangeLongSpliterator(
                min+offset, max, reverse);
    }

    @Override
    public Spliterator<Long> end(long num) {
        return new ReversingRangeLongSpliterator(
                start, start+num, reverse);
    }


}