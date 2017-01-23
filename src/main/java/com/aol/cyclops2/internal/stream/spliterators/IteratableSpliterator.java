package com.aol.cyclops2.internal.stream.spliterators;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */

public class IteratableSpliterator<T> extends Spliterators.AbstractSpliterator<T> implements CopyableSpliterator<T>{

    private final Iterable<T> source;

    Iterator<T> active;

    public IteratableSpliterator(final Iterable<T> source) {
        super(-1,Spliterator.ORDERED);

        this.source = source;


    }
    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        if(active==null)
            active=source.iterator();

        active.forEachRemaining(action);

    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {

        if(active==null)
            active=source.iterator();
        if (active.hasNext()) {
            action.accept(active.next());
            return true;
        }

        return false;
    }

    @Override
    public Spliterator<T> copy() {

      System.out.println("Source " + source);
        return new IteratableSpliterator<>(source);
    }
}
