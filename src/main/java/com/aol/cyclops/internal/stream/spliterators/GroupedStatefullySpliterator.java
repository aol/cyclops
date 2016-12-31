package com.aol.cyclops.internal.stream.spliterators;

import cyclops.collections.ListX;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class GroupedStatefullySpliterator<T, C extends Collection<? super T>,R> extends Spliterators.AbstractSpliterator<R>
                                implements CopyableSpliterator<R>,ComposableFunction<R,T,GroupedStatefullySpliterator<T,C,?>> {
    private final Spliterator<T> source;
    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    final BiPredicate<? super C, ? super T> predicate;
    public GroupedStatefullySpliterator(final Spliterator<T> source,
                                        Supplier<? extends C> factory,
                                        Function<? super C, ? extends R> finalizer,
                                        BiPredicate<? super C, ? super T> predicate) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.factory = factory;
        this.finalizer=finalizer;
        this.predicate = predicate;
        collection =factory.get();



    }
    public <R2> GroupedStatefullySpliterator<T,C,?> compose(Function<? super R,? extends R2> fn){
        return new GroupedStatefullySpliterator<T, C,R2>(CopyableSpliterator.copy(source),factory,finalizer.andThen(fn),predicate);
    }

    C collection;

    @Override
    public void forEachRemaining(Consumer<? super R> action) {

        source.forEachRemaining(t->{

            collection.add(t);

            if(!predicate.test(collection,t)) {
                if (collection.size() > 0) {
                    action.accept(finalizer.apply(collection));

                    collection = factory.get();
                }

            }

        });
        if(collection.size()>0){
            action.accept(finalizer.apply(collection));
        }

    }

    boolean closed =false;
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        if(closed)
            return false;

        boolean accepted[]= {true};
        while (accepted[0]) {
            boolean canAdvance = source.tryAdvance(t -> {
                collection.add(t);
                accepted[0]=  predicate.test(collection,t);
            });
            if (!canAdvance) {
                if(collection.size()>0) {
                    action.accept(finalizer.apply(collection));

                    collection = factory.get();
                }
                closed = true;
                return false;
            }
        }

        if(collection.size()>0) {
            action.accept(finalizer.apply(collection));
            collection = factory.get();
        }

        return true;
    }

    @Override
    public Spliterator<R> copy() {
        return new GroupedStatefullySpliterator<T, C,R>(CopyableSpliterator.copy(source),factory,finalizer,predicate);
    }


}
