package com.aol.cyclops.internal.stream.spliterators;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class GroupingSpliterator<T, C extends Collection<? super T>> extends Spliterators.AbstractSpliterator<C> implements CopyableSpliterator<C> {
    private final Spliterator<T> source;
    private final Supplier<? extends C> factory;
    private final int groupSize;
    public GroupingSpliterator(final Spliterator<T> source, Supplier<? extends C> factory, int groupSize) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.factory = factory;
        this.groupSize = groupSize;
        collection =factory.get();


    }

    C collection;
    @Override
    public void forEachRemaining(Consumer<? super C> action) {

        source.forEachRemaining(t->{
            collection.add(t);
            if(collection.size()==groupSize){
                action.accept(collection);
                collection = factory.get();
            }

        });

    }

    @Override
    public boolean tryAdvance(Consumer<? super C> action) {
        for(int i=collection.size();i<groupSize;i++) {
            boolean canAdvance = source.tryAdvance(t -> {
                collection.add(t);
            });
            if (!canAdvance) {
                action.accept(collection);
                collection = factory.get();
                return false;
            }
        }
        action.accept(collection);
        collection = factory.get();
        return true;
    }

    @Override
    public Spliterator<C> copy() {
        return new GroupingSpliterator<T, C>(CopyableSpliterator.copy(source),factory,groupSize);
    }
}
