package com.oath.cyclops.internal.stream.spliterators.longs;

import com.oath.cyclops.internal.stream.spliterators.ReversableSpliterator;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.LongConsumer;

//@AllArgsConstructor
public class ReversingLongArraySpliterator<Long> implements Spliterator.OfLong,ReversableSpliterator<java.lang.Long> {

    private final long[] array;
    private int max;
    private int start;
    @Getter
    @Setter
    private boolean reverse;

    int index = 0;

    public ReversingLongArraySpliterator(long[] array, int start, int max, boolean reverse) {
        this.array = array;
        this.reverse=reverse;

        this.max = Math.min(array.length,max);
        this.start= Math.max(0,start);
        this.index = calcIndex();
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
    public ReversingLongArraySpliterator<Long> invert() {

        setReverse(!isReverse());
        index = calcIndex();
        return this;
    }

    private int calcIndex() {
        if(isReverse()) {
            return max - 1;
        }else{
            return start;
        }
    }

    @Override
    public boolean tryAdvance(Consumer<? super java.lang.Long> action) {
        Objects.requireNonNull(action);



        if (!reverse) {
            if (index < max && index > -1) {
                action.accept(array[index++]);
                return true;
            }
        } else {
            if (index > (start-1) & index < max) {
                action.accept( array[index--]);
                return true;
            }
        }
        return false;

    }

    @Override
    public void forEachRemaining(Consumer<? super java.lang.Long> action) {
        Objects.requireNonNull(action);

        int index = this.index; //local index for replayability

        if (!reverse) {
            for (;index < max && index > -1;) {
                action.accept(array[index++]);

            }
        } else {
            for (;index > (start-1) & index < max;) {
                action.accept(array[index--]);

            }
        }
    }

    @Override
    public OfLong trySplit() {
        return this;
    }




    @Override
    public boolean tryAdvance(LongConsumer action) {
        Objects.requireNonNull(action);



        if (!reverse) {
            if (index < max && index > -1) {
                action.accept( array[index++]);
                return true;
            }
        } else {
            if (index > (start-1) & index < max) {
                action.accept( array[index--]);
                return true;
            }
        }
        return false;

    }

    @Override
    public void forEachRemaining(LongConsumer action) {
        Objects.requireNonNull(action);

        int index = this.index; //local index for replayability

        if (!reverse) {
            for (;index < max && index > -1;) {
                action.accept(array[index++]);

            }
        } else {
            for (;index > (start-1) & index < max;) {
                action.accept(array[index--]);

            }
        }
    }

    @Override
    public ReversableSpliterator<java.lang.Long> copy() {
        return new ReversingLongArraySpliterator<>(
                array, start,max,reverse);
    }



}
