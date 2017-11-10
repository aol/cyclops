package com.oath.cyclops.internal.stream.spliterators;

import cyclops.function.Function3;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Created by johnmcclean on 15/12/2016.
 */
//@AllArgsConstructor
public class Zipping3Spliterator<T1,T2,T3,R> implements CopyableSpliterator<R>,
                                            ComposableFunction<R,T1,Zipping3Spliterator<T1,T2,T3,?>> {
    private final Spliterator<T1> left;

    private final Spliterator<T2> middle;
    private final Spliterator<T3> right;
    private final Function3<? super T1, ? super T2, ? super T3, ? extends R> fn;


    public Zipping3Spliterator(Spliterator<T1> left, Spliterator<T2> middle,Spliterator<T3> right, Function3<? super T1, ? super T2, ? super T3, ? extends R> fn) {
        this.left = copy(left);
        this.middle = copy(middle);
        this.right = copy(right);
        this.fn = fn;
    }
    public <R2> Zipping3Spliterator<T1,T2,T3,R2> compose(Function<? super R,? extends R2> fn){
        return new Zipping3Spliterator<>(copy(left), copy(middle), copy(right),
                this.fn.andThen3(fn));
    }


    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
         boolean found[] = {false};
         return left.tryAdvance(l ->
                middle.tryAdvance(m -> {
                    right.tryAdvance(r-> {
                        action.accept(fn.apply(l,m, r));
                        found[0] = true;
                    });
                })) && found[0];

    }



    @Override
    public Spliterator<R> copy() {
        return new Zipping3Spliterator(copy(left),
                                        copy(middle), copy(right),fn);
    }

    @Override
    public Spliterator<R> trySplit() {
        return this;
    }


    @Override
    public long estimateSize() {
        return Math.min(left.estimateSize(), Math.min(middle.estimateSize(),right.estimateSize()));
    }


    @Override
    public int characteristics() {
        return left.characteristics() & middle.characteristics()& right.characteristics() & ~(SORTED | DISTINCT);
    }
}
