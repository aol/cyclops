package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

//@AllArgsConstructor
public class ReversingArraySpliterator<T> implements Spliterator<T>, ReversableSpliterator<T>, Indexable<T> {

    private final Object[] array;
    private final int max;
    private final int start;
    @Getter
    @Setter
    private boolean reverse;

    int index = 0;

    public ReversingArraySpliterator(Object[] array,int start,int max,boolean reverse) {
        this.array = array;
        this.reverse=reverse;
        this.max = max;
        this.start= start;
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
    public ReversingArraySpliterator<T> invert() {

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
    public void forEachRemaining(Consumer<? super T> action) {
        Objects.requireNonNull(action);

        int index = this.index; //local index for replayability

        if (!reverse) {
            for (;index < max && index > -1;) {
                action.accept((T) array[index++]);

            }
        } else {
            for (;index > (start-1) & index < max;) {
                action.accept((T) array[index--]);

            }
        }

    }

    @Override
    public boolean tryAdvance(final Consumer<? super T> action) {
        Objects.requireNonNull(action);



        if (!reverse) {
            if (index < max && index > -1) {
                action.accept((T) array[index++]);
                return true;
            }
        } else {
            if (index > (start-1) & index < max) {
                action.accept((T) array[index--]);
                return true;
            }
        }
        return false;

    }

    @Override
    public Spliterator<T> trySplit() {

        return this;
    }

    @Override
    public ReversableSpliterator<T> copy() {

        return new ReversingArraySpliterator<T>(
                                                array, start,max,reverse);
    }

    @Override
    public Spliterator<T> start(long start) {
        return new ReversingArraySpliterator<T>(
                array, (int)start, max,reverse);
    }

    @Override
    public Spliterator<T> end(long end) {
        return new ReversingArraySpliterator<T>(
                array, start, (int)end,reverse);
    }

    @Override
    public Spliterator<T> startAndEnd(long start, long end) {
        return new ReversingArraySpliterator<T>(
                array, (int)start, (int)end,reverse);
    }
}
