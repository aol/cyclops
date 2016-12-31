package com.aol.cyclops.internal.stream.spliterators;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.*;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class GroupedWhileSpliterator<T, C extends Collection<? super T>,R> extends Spliterators.AbstractSpliterator<R>
                                implements CopyableSpliterator<R>,ComposableFunction<R,T,GroupedWhileSpliterator<T,C,?>> {
    private final Spliterator<T> source;
    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    final Predicate<? super T> predicate;
    public GroupedWhileSpliterator(final Spliterator<T> source,
                                   Supplier<? extends C> factory,
                                   Function<? super C, ? extends R> finalizer,
                                   Predicate<? super T> predicate) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.factory = factory;
        this.finalizer=finalizer;
        this.predicate = predicate;
        collection =factory.get();



    }
    public <R2> GroupedWhileSpliterator<T,C,?> compose(Function<? super R,? extends R2> fn){
        return new GroupedWhileSpliterator<T, C,R2>(CopyableSpliterator.copy(source),factory,finalizer.andThen(fn),predicate);
    }

    C collection;

    @Override
    public void forEachRemaining(Consumer<? super R> action) {

        source.forEachRemaining(t->{

            collection.add(t);

            if(!predicate.test(t)){
                action.accept(finalizer.apply(collection));

                collection = factory.get();

            }else{

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

                accepted[0]=  predicate.test(t);
            });
            if (!canAdvance) {
                if(collection.size()>0){
                    action.accept(finalizer.apply(collection));

                    collection = factory.get();
                }
                closed = true;
                return false;
            }
        }

        if(collection.size()>0){

            action.accept(finalizer.apply(collection));

            collection = factory.get();
        }

        return true;
    }

    @Override
    public Spliterator<R> copy() {
        return new GroupedWhileSpliterator<T, C,R>(CopyableSpliterator.copy(source),factory,finalizer,predicate);
    }


}
