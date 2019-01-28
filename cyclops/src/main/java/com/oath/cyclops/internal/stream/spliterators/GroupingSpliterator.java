package com.oath.cyclops.internal.stream.spliterators;

import com.oath.cyclops.types.persistent.PersistentCollection;

import java.util.Collection;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Created by johnmcclean on 22/12/2016.
 */
public class GroupingSpliterator<T, C extends PersistentCollection<? super T>,R> extends Spliterators.AbstractSpliterator<R>
                                implements CopyableSpliterator<R>,ComposableFunction<R,T,GroupingSpliterator<T,C,?>> {
    private final Spliterator<T> source;
    private final Supplier<? extends C> factory;
    private final Function<? super C, ? extends R> finalizer;
    private final int groupSize;
    public GroupingSpliterator(final Spliterator<T> source, Supplier<? extends C> factory, Function<? super C, ? extends R> finalizer,int groupSize) {
        super(source.estimateSize(),source.characteristics() & Spliterator.ORDERED);

        this.source = source;
        this.factory = factory;
        this.groupSize = groupSize >0 ? groupSize : 1;
        this.finalizer=finalizer;
        collection =factory.get();


    }
    public <R2> GroupingSpliterator<T,C,?> compose(Function<? super R,? extends R2> fn){
        return new GroupingSpliterator<T, C,R2>(CopyableSpliterator.copy(source),factory,finalizer.andThen(fn),groupSize);
    }

    C collection;
    boolean sent = false;
    boolean data = false;
    @Override
    public void forEachRemaining(Consumer<? super R> action) {

        source.forEachRemaining(t->{
            if(data==false)
                data = true;
            collection = (C)collection.plus(t);

            if(collection.size()==groupSize){
                action.accept(finalizer.apply(collection));
                sent = true;
                collection = factory.get();
            }else{
                sent = false;
            }

        });
        if(data && !sent){
            action.accept(finalizer.apply(collection));
        }

    }

    boolean closed =false;

    @Override
    public boolean tryAdvance(Consumer<? super R> action) {
        if(closed)
            return false;
        for(int i=collection.size();collection.size()<groupSize;i++) {
            boolean canAdvance = source.tryAdvance(t -> {
                collection = (C)collection.plus(t);

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
        return new GroupingSpliterator<T, C,R>(CopyableSpliterator.copy(source),factory,finalizer,groupSize);
    }


}
