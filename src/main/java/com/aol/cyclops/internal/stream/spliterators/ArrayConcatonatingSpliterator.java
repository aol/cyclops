package com.aol.cyclops.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class ArrayConcatonatingSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T>{
    private final Spliterator<T>[] spliterators;
    private int index =0 ;
    public ArrayConcatonatingSpliterator(Spliterator<T>... spliterators){
        super(size(spliterators),calcCharacteristics(spliterators));
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
        for(Spliterator<T> next : spliterators){
            next.forEachRemaining(action);
        }
    }

    @Override
    public Spliterator<T> copy() {
        return new ArrayConcatonatingSpliterator<T>(spliterators);
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        while(index < spliterators.length){
            if(!spliterators[index].tryAdvance(action)){
                return true;
            }else{
                index++;
            }
        }
        return false;
    }
}
