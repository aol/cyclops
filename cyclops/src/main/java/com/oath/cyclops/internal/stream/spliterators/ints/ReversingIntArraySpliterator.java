package com.oath.cyclops.internal.stream.spliterators.ints;


import com.oath.cyclops.internal.stream.spliterators.ReversableSpliterator;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

//@AllArgsConstructor
public class ReversingIntArraySpliterator<Integer> implements Spliterator.OfInt,ReversableSpliterator<java.lang.Integer> {

    private final int[] array;
    private int max;
    private int start;
    @Getter
    @Setter
    private boolean reverse;

    int index = 0;

    public ReversingIntArraySpliterator(int[] array, int start, int max, boolean reverse) {
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
    public ReversingIntArraySpliterator<Integer> invert() {

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
    public boolean tryAdvance(Consumer<? super java.lang.Integer> action) {
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
    public void forEachRemaining(Consumer<? super java.lang.Integer> action) {
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
    public OfInt trySplit() {
        return this;
    }





    @Override
    public boolean tryAdvance(IntConsumer action) {
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
    public void forEachRemaining(IntConsumer action) {
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
    public ReversableSpliterator<java.lang.Integer> copy() {
        return new ReversingIntArraySpliterator<>(
                array, start,max,reverse);
    }



}
