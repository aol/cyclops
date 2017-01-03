package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class ArrayConcatonatingSpliterator<IN,T> extends BaseComposableSpliterator<IN,T,ArrayConcatonatingSpliterator<IN,?>>
                                                implements CopyableSpliterator<T>{
    private final Spliterator<IN>[] spliterators;
    private int index =0 ;


    public ArrayConcatonatingSpliterator(Spliterator<IN>... spliterators){
        super(size(spliterators),calcCharacteristics(spliterators),null);
        this.spliterators =spliterators;
    }
    public ArrayConcatonatingSpliterator(Function<? super IN, ? extends T> fn,Spliterator<IN>... spliterators){
        super(size(spliterators),calcCharacteristics(spliterators),fn);
        this.spliterators =spliterators;
    }

    private static <T> int calcCharacteristics(Spliterator<T>[] spliterators) {
        int chars = spliterators[0].characteristics();
        for(int i=1;i<spliterators.length;i++){
            chars = chars & spliterators[i].characteristics();
        }
        return chars &  (ORDERED | SIZED | SUBSIZED);
    }

    private static <T> long size(Spliterator<T>[] spliterators) {
        long size= 0;
        for(Spliterator<T> next : spliterators){
            size += next.estimateSize();
        }
        return size;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        for(Spliterator<IN> next : spliterators){
            next.forEachRemaining(apply(action));
        }
    }

    @Override
    public Spliterator<T> copy() {
        return new ArrayConcatonatingSpliterator<IN,T>(fn,CopyableSpliterator.copy(spliterators));
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        while(index < spliterators.length){
            if(spliterators[index].tryAdvance(apply(action))){
                return true;
            }else{
                index++;
            }
        }
        return false;
    }

    @Override
    <R2> ArrayConcatonatingSpliterator<IN,?> create(Function<? super IN, ? extends R2> after) {
        return new ArrayConcatonatingSpliterator(after,spliterators);
    }
}
