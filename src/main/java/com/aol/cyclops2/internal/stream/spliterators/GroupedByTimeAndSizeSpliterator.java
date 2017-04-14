package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class GroupedByTimeAndSizeSpliterator<T, C extends Collection<? super T>,R> extends Spliterators.AbstractSpliterator<R>
                                implements CopyableSpliterator<R>,ComposableFunction<R,T,GroupedByTimeAndSizeSpliterator<T,C,?>> {
    private final Spliterator<T> source;
    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    private final int groupSize;
    private final long time;
    private final TimeUnit t;
    final long toRun;
    public GroupedByTimeAndSizeSpliterator(final Spliterator<T> source,
                                           Supplier<? extends C> factory,
                                           Function<? super C, ? extends R> finalizer,
                                           int groupSize,
                                           long time,
                                           TimeUnit t) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);
        if(groupSize<=0)
            throw new IllegalArgumentException("Group size must be greater than 0");
        this.source = source;
        this.factory = factory;
        this.groupSize = groupSize;
        this.finalizer=finalizer;
        this.time = time;
        this.t = t;
        collection =factory.get();
        toRun =t.toNanos(time);


    }
    public <R2> GroupedByTimeAndSizeSpliterator<T,C,?> compose(Function<? super R,? extends R2> fn){
        return new GroupedByTimeAndSizeSpliterator<T, C,R2>(CopyableSpliterator.copy(source),factory,finalizer.andThen(fn),groupSize,time,t);
    }

    C collection;

    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        start = System.nanoTime();
        source.forEachRemaining(t->{


            collection.add(t);

            if(collection.size()==groupSize || System.nanoTime() - start >= toRun){

                start = System.nanoTime();

                action.accept(finalizer.apply(collection));

                collection = factory.get();

            }



        });
        if(collection.size()>0){
            action.accept(finalizer.apply(collection));
        }

    }
    long start = -1;
    boolean closed =false;
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        System.out.println("Current time  " + System.currentTimeMillis());
        if(closed)
            return false;
        if(start ==-1 )
            start = System.nanoTime();

        while (System.nanoTime() - start < toRun  && collection.size() < groupSize) {
            boolean canAdvance = source.tryAdvance(t -> {

                collection.add(t);
            });
            if (!canAdvance) {
                if(collection.size()>0) {
                    action.accept(finalizer.apply(collection));
                    start = System.nanoTime();
                    collection = factory.get();
                }
                closed = true;
                return false;
            }
            
        }
        System.out.println("Reseting!");

        if(collection.size()>0) {
            action.accept(finalizer.apply(collection));
            collection = factory.get();
        }
        start = System.nanoTime();
        return true;
    }

    @Override
    public Spliterator<R> copy() {
        return new GroupedByTimeAndSizeSpliterator<T, C,R>(CopyableSpliterator.copy(source),factory,finalizer,groupSize,time,t);
    }


}
