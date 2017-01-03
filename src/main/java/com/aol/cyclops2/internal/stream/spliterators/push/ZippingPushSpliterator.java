package com.aol.cyclops2.internal.stream.spliterators.push;

import com.aol.cyclops2.internal.stream.spliterators.ComposableFunction;
import com.aol.cyclops2.internal.stream.spliterators.CopyableSpliterator;

import java.util.ArrayDeque;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 15/12/2016.
 */
//@AllArgsConstructor
public class ZippingPushSpliterator<T1,T2,R> implements CopyableSpliterator<R>,
                                            ComposableFunction<R,T1,ZippingPushSpliterator<T1,T2,?>> {
    private final Spliterator<T1> left;
    private final Spliterator<T2> right;
    private final BiFunction<? super T1, ? super T2, ? extends R> fn;
    //with push based Streams values could arrive at any stage
    private final QueueConsumer<T1> leftQueue = new QueueConsumer<T1>();
    private final QueueConsumer<T2> rightQueue = new QueueConsumer<T2>();

    public ZippingPushSpliterator(Spliterator<T1> left, Spliterator<T2> right, BiFunction<? super T1, ? super T2, ? extends R> fn) {
        this.left = CopyableSpliterator.copy(left);
        this.right = CopyableSpliterator.copy(right);
        this.fn = fn;
    }
    public <R2> ZippingPushSpliterator<T1,T2,R2> compose(Function<? super R,? extends R2> fn){
        return new ZippingPushSpliterator<>(CopyableSpliterator.copy(left),CopyableSpliterator.copy(right),
                this.fn.andThen(fn));
    }
    static class QueueConsumer<T> implements Consumer<T>{

        T value;
        ArrayDeque<T> q;

        @Override
        public void accept(T t) {
            System.out.println("Zipper " + t);
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
        left.tryAdvance(leftQueue);
        right.tryAdvance(rightQueue);

        if(!leftQueue.isEmpty() && !rightQueue.isEmpty()){
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
            CopyableSpliterator.super.forEachRemaining(action);
            return;
        }


    }

    @Override
    public Spliterator<R> copy() {
        return new ZippingPushSpliterator(CopyableSpliterator.copy(left),
                                        CopyableSpliterator.copy(right),fn);
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
