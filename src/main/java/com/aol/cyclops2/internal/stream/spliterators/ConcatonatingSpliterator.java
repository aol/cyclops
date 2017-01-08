package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class ConcatonatingSpliterator<IN,T> extends BaseComposableSpliterator<IN,T,ConcatonatingSpliterator<IN,?>>
                                                implements CopyableSpliterator<T>{
    private final Spliterator<IN> left;
    private final Spliterator<IN> right;
    private boolean isRight= false;


    public ConcatonatingSpliterator(Spliterator<IN> left,Spliterator<IN> right){
        super(size(left,right),calcCharacteristics(left,right),null);
        this.left = left;
        this.right = right;
    }
    public ConcatonatingSpliterator(Function<? super IN, ? extends T> fn, Spliterator<IN> left,Spliterator<IN> right){
        super(size(left,right),calcCharacteristics(left,right),fn);
        this.left = left;
        this.right = right;
    }

    private static <T> int calcCharacteristics(Spliterator<T>... spliterators) {
        int chars = spliterators[0].characteristics();
        for(int i=1;i<spliterators.length;i++){
            chars = chars & spliterators[i].characteristics();
        }
        return chars &  (ORDERED | SIZED | SUBSIZED);
    }

    private static <T> long size(Spliterator<T>... spliterators) {

        long size= 0;
        for(Spliterator<T> next : spliterators){
            size += next.estimateSize();
        }
        return size;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        Consumer<? super IN> toUse = apply(action);
        left.forEachRemaining(toUse);
        right.forEachRemaining(toUse);
    }

    @Override
    public Spliterator<T> copy() {
        return new ConcatonatingSpliterator<IN,T>(fn,CopyableSpliterator.copy(left),CopyableSpliterator.copy(right));
    }

    boolean rightAdvance(Consumer<? super IN> action) {
        isRight = true;
        return right.tryAdvance(action);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        Consumer<? super IN> toUse = apply(action);
        if (!isRight) {
            return left.tryAdvance(toUse) ? true : rightAdvance(toUse);
        }
        else
            return right.tryAdvance(toUse);

    }

    @Override
    <R2> ConcatonatingSpliterator<IN,?> create(Function<? super IN, ? extends R2> after) {
        return new ConcatonatingSpliterator(after,left,right);
    }
}
