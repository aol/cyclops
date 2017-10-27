package com.oath.cyclops.internal.stream.spliterators.doubles;

import com.oath.cyclops.internal.stream.spliterators.Indexable;
import com.oath.cyclops.internal.stream.spliterators.ReversableSpliterator;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.DoubleConsumer;

//@AllArgsConstructor
public class ReversingDoubleArraySpliterator<Double> implements Spliterator.OfDouble,ReversableSpliterator<java.lang.Double>, Indexable<java.lang.Double> {

    private final double[] array;
    private int max;
    private int start;
    @Getter
    @Setter
    private boolean reverse;

    int index = 0;

    public ReversingDoubleArraySpliterator(double[] array, int start, int max, boolean reverse) {
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
    public ReversingDoubleArraySpliterator<Double> invert() {

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
    public boolean tryAdvance(Consumer<? super java.lang.Double> action) {
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
    public void forEachRemaining(Consumer<? super java.lang.Double> action) {
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
    public OfDouble trySplit() {
        return this;
    }





    @Override
    public boolean tryAdvance(DoubleConsumer action) {
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
    public void forEachRemaining(DoubleConsumer action) {
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
    public ReversableSpliterator<java.lang.Double> copy() {
        return new ReversingDoubleArraySpliterator<>(
                array, start,max,reverse);
    }

    @Override
    public Spliterator<java.lang.Double> skip(long offset) {
        this.start = index= this.start+(int)offset;
        return this;
    }

    @Override
    public Spliterator<java.lang.Double> take(long num) {
        this.max = start+(int)num;
        return this;
    }

}
