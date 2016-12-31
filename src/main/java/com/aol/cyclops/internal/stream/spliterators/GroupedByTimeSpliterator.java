package com.aol.cyclops.internal.stream.spliterators;

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
public class GroupedByTimeSpliterator<T, C extends Collection<? super T>,R> extends Spliterators.AbstractSpliterator<R>
                                implements CopyableSpliterator<R>,ComposableFunction<R,T,GroupedByTimeSpliterator<T,C,?>> {
    private final Spliterator<T> source;
    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    private final long time;
    private final TimeUnit t;
    final long toRun;
    public GroupedByTimeSpliterator(final Spliterator<T> source,
                                    Supplier<? extends C> factory,
                                    Function<? super C, ? extends R> finalizer,
                                    long time,
                                    TimeUnit t) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.factory = factory;
        this.finalizer=finalizer;
        this.time = time;
        this.t = t;
        collection =factory.get();
        toRun =t.toNanos(time);


    }
    public <R2> GroupedByTimeSpliterator<T,C,?> compose(Function<? super R,? extends R2> fn){
        return new GroupedByTimeSpliterator<T, C,R2>(CopyableSpliterator.copy(source),factory,finalizer.andThen(fn),time,t);
    }

    C collection;
    boolean sent = false;
    boolean data = false;
    @Override
    public void forEachRemaining(Consumer<? super R> action) {
        start = System.nanoTime();
        source.forEachRemaining(t->{
            if(data==false)
                data = true;
            collection.add(t);

            if(System.nanoTime() - start >= toRun){
                action.accept(finalizer.apply(collection));
                sent = true;
                collection = factory.get();
                start = System.nanoTime();
            }else{
                sent = false;
            }

        });
        if(data && !sent){
            action.accept(finalizer.apply(collection));
        }

    }
    long start = -1;
    boolean closed =false;
    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        if(closed)
            return false;
        if(start ==-1 )
            start = System.nanoTime();

       do  {
            boolean canAdvance = source.tryAdvance(t -> {
                collection.add(t);
            });
            if (!canAdvance) {
                action.accept(finalizer.apply(collection));
                start = System.nanoTime();
                collection = factory.get();
                closed = true;
                return false;
            }
        }while(System.nanoTime() - start < toRun);


        action.accept(finalizer.apply(collection));
        collection = factory.get();
        start = System.nanoTime();
        return true;
    }

    @Override
    public Spliterator<R> copy() {
        return new GroupedByTimeSpliterator<T, C,R>(CopyableSpliterator.copy(source),factory,finalizer,time,t);
    }


}
