package com.aol.cyclops.internal.stream.spliterators;

import lombok.AllArgsConstructor;

import java.util.ArrayDeque;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 15/12/2016.
 */
@AllArgsConstructor
public class ZippingSpliterator<T1,T2,R> implements Spliterator<R> {
    private final Spliterator<T1> left;
    private final Spliterator<T2> right;
    private final BiFunction<? super T1, ? super T2, ? extends R> fn;
    //with push based Streams values could arrive at any stage
    private final QueueConsumer<T1> leftQueue = new QueueConsumer<T1>();
    private final QueueConsumer<T2> rightQueue = new QueueConsumer<T2>();


    static class QueueConsumer<T> implements Consumer<T>{

        T value;
        ArrayDeque<T> q;

        @Override
        public void accept(T t) {
            if(value==null){
                value = t;
            }else{
                if(q==null){
                    q= new ArrayDeque<>();
                }
                q.offer(t);
            }
        }
        public boolean isEmpty(){
            return value==null;
        }
        public T next(){
            T res = value;
            if(q!=null && q.size()>0)
                value = q.pop();
            else
                value =null;
            return res;
        }
    }



    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        if(left.tryAdvance(leftQueue) && right.tryAdvance(rightQueue)){
            while(!leftQueue.isEmpty() && !rightQueue.isEmpty()){
                action.accept(fn.apply(leftQueue.next(),rightQueue.next()));
            }
            return true;
        }
        return false;
    }

    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        if(hasCharacteristics(SIZED)) {
            if(left.getExactSizeIfKnown() < right.getExactSizeIfKnown()){
                left.forEachRemaining(next -> {
                    if(right.tryAdvance(rightQueue)) {
                        action.accept(fn.apply(next, rightQueue.next()));
                    }
                });
            }else{
                right.forEachRemaining(next -> {
                    if(left.tryAdvance(leftQueue)) {
                        action.accept(fn.apply(leftQueue.next(), next));
                    }
                });
            }

        }else{
            Spliterator.super.forEachRemaining(action);
            return;
        }


    }

    @Override
    public Spliterator<R> trySplit() {
        return this;
    }


    @Override
    public long estimateSize() {
        return Math.min(left.estimateSize(),right.estimateSize());
    }


    @Override
    public int characteristics() {
        return left.characteristics() & right.characteristics() & ~(Spliterator.SORTED | Spliterator.DISTINCT);
    }
}
