package com.oath.cyclops.internal.stream.spliterators;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;

import lombok.Getter;
import lombok.Setter;

//@AllArgsConstructor
public class ReversingArraySpliterator<T> implements Spliterator<T>, ReversableSpliterator<T>, Indexable<T> {

    private final Object[] array;
    private int max;
    private int start;
    @Getter
    @Setter
    private boolean reverse;

    int index = 0;

    public ReversingArraySpliterator(Object[] array,int start,int max,boolean reverse) {
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
    public ReversingArraySpliterator<T> invert() {
        return new ReversingArraySpliterator<T>(array,start,max,!reverse);

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
    public Spliterator<T> skip(long offset) {
    this.start = index= this.start+(int)offset;
        return new ReversingArraySpliterator<T>(
                array, start, max,reverse);


    }

    @Override
    public Spliterator<T> take(long num) {
        this.max = start+(int)num;
        return new ReversingArraySpliterator<T>(
                array, start, max,reverse);
    }


}
