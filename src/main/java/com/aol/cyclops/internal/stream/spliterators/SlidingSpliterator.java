package com.aol.cyclops.internal.stream.spliterators;

import cyclops.box.Mutable;
import cyclops.collections.ListX;
import cyclops.collections.immutable.PStackX;
import cyclops.collections.immutable.PVectorX;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;
import org.pcollections.PVector;
import org.pcollections.TreePVector;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class SlidingSpliterator<T,R> extends Spliterators.AbstractSpliterator<R>
                                implements CopyableSpliterator<R>,ComposableFunction<R,T,SlidingSpliterator<T,?>> {
    private final Spliterator<T> source;

    private final Function<? super PVectorX<T>, ? extends R> finalizer;
    private final int windowSize;
    private final int increment;
    final Mutable<PVector<T>> list = Mutable.of(TreePVector.empty());
    public SlidingSpliterator(final Spliterator<T> source,  Function<? super PVectorX<T>, ? extends R> finalizer,
                                int windowSize, int increment) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;

        this.windowSize = windowSize;
        this.finalizer=finalizer;
        this.increment=increment;



    }
    public <R2> SlidingSpliterator<T,?> compose(Function<? super R,? extends R2> fn){
        return new SlidingSpliterator<T,R2>(CopyableSpliterator.copy(source),finalizer.andThen(fn), windowSize,increment);
    }


    @Override
    public void forEachRemaining(Consumer<? super R> action) {

        source.forEachRemaining(t->{
            list.mutate(var -> var.plus(Math.max(0, var.size()), t));
            if(list.get().size()==windowSize){
                action.accept(finalizer.apply(PVectorX.fromIterable(list.get())));
                list.mutate(var -> var.minus(0));
            }


        });

    }

    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        boolean canAdvance = true;
        for (int i = 0; i < increment && list.get()
                .size() > 0; i++)
            list.mutate(var -> var.minus(0));
        for (; list.get()
                .size() < windowSize
                && canAdvance;) {
            Mutable<T> box = Mutable.of(null);
            canAdvance = source.tryAdvance(t -> {
                box.accept(t);
            });
            if (canAdvance) {
                list.mutate(var -> var.plus(Math.max(0, var.size()), box.get()));
            }

        }
        action.accept(finalizer.apply(PVectorX.fromIterable(list.get())));

        return canAdvance;
    }

    @Override
    public Spliterator<R> copy() {
        return new SlidingSpliterator<T, R>(CopyableSpliterator.copy(source),finalizer, windowSize,increment);
    }


}
