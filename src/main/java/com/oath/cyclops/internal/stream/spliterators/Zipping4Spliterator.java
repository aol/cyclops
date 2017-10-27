package com.oath.cyclops.internal.stream.spliterators;

import cyclops.function.Function4;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 15/12/2016.
 */
//@AllArgsConstructor
public class Zipping4Spliterator<T1,T2,T3,T4,R> implements CopyableSpliterator<R>,
                                            ComposableFunction<R,T1,Zipping4Spliterator<T1,T2,T3,T4,?>> {
    private final Spliterator<T1> left;

    private final Spliterator<T2> middle;
    private final Spliterator<T3> middle2;
    private final Spliterator<T4> right;
    private final Function4<? super T1, ? super T2, ? super T3, ? super T4,? extends R> fn;


    public Zipping4Spliterator(Spliterator<T1> left, Spliterator<T2> middle, Spliterator<T3> middle2, Spliterator<T4> right,Function4<? super T1, ? super T2, ? super T3, ? super T4, ? extends R> fn) {
        this.left = left;
        this.middle = middle;
        this.middle2 = middle2;
        this.right = right;
        this.fn = fn;
    }
    public <R2> Zipping4Spliterator<T1,T2,T3,T4,R2> compose(Function<? super R,? extends R2> fn){
        return new Zipping4Spliterator<>(CopyableSpliterator.copy(left),CopyableSpliterator.copy(middle),CopyableSpliterator.copy(middle2),
                CopyableSpliterator.copy(right),
                this.fn.andThen4(fn));
    }


    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
         boolean found[] = {false};
         return left.tryAdvance(l ->
                middle.tryAdvance(m -> {
                    middle2.tryAdvance(m2-> {
                        right.tryAdvance(r-> {
                                    action.accept(fn.apply(l,m,m2, r));
                                    found[0] = true;
                                });

                    });
                })) && found[0];

    }



    @Override
    public Spliterator<R> copy() {
        return new Zipping4Spliterator(CopyableSpliterator.copy(left),
                                        CopyableSpliterator.copy(middle),CopyableSpliterator.copy(middle2),
                                        CopyableSpliterator.copy(right),fn);
    }

    @Override
    public Spliterator<R> trySplit() {
        return this;
    }


    @Override
    public long estimateSize() {
        return Math.min(left.estimateSize(), Math.min(middle.estimateSize(), Math.min(middle2.estimateSize(),right.estimateSize())));
    }


    @Override
    public int characteristics() {
        return left.characteristics() & middle.characteristics()& middle2.characteristics() & right.characteristics() & ~(Spliterator.SORTED | Spliterator.DISTINCT);
    }
}
