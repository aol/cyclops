package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 15/12/2016.
 */
//@AllArgsConstructor
public class ZippingSpliterator<T1,T2,R> implements CopyableSpliterator<R>,
                                            ComposableFunction<R,T1,ZippingSpliterator<T1,T2,?>> {
    private final Spliterator<T1> left;
    private final Spliterator<T2> right;
    private final BiFunction<? super T1, ? super T2, ? extends R> fn;


    public ZippingSpliterator(Spliterator<T1> left, Spliterator<T2> right, BiFunction<? super T1, ? super T2, ? extends R> fn) {
        this.left = CopyableSpliterator.copy(left);
        this.right = CopyableSpliterator.copy(right);
        this.fn = fn;
    }
    public <R2> ZippingSpliterator<T1,T2,R2> compose(Function<? super R,? extends R2> fn){
        return new ZippingSpliterator<>(CopyableSpliterator.copy(left),CopyableSpliterator.copy(right),
                this.fn.andThen(fn));
    }


    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
         boolean found[] = {false};
         return left.tryAdvance(l ->
                right.tryAdvance(r -> {
                    action.accept(fn.apply(l, r));
                    found[0]=true;
                })) && found[0];

    }

    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        if(hasCharacteristics(SIZED)) {
            if(left.getExactSizeIfKnown() < right.getExactSizeIfKnown()){
                left.forEachRemaining(l -> {
                    right.tryAdvance(r -> {
                        action.accept(fn.apply(l, r));

                    });
                });
            }else {
                right.forEachRemaining(r -> {
                    left.tryAdvance(l -> {
                        action.accept(fn.apply(l, r));

                    });
                });
            }

        }else{
            CopyableSpliterator.super.forEachRemaining(action);
            return;
        }


    }

    @Override
    public Spliterator<R> copy() {
        return new ZippingSpliterator(CopyableSpliterator.copy(left),
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
