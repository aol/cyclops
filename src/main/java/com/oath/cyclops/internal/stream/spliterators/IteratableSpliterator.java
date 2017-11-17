package com.oath.cyclops.internal.stream.spliterators;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;

/**
 * Created by johnmcclean on 22/12/2016.
 */

public class IteratableSpliterator<T> implements Spliterator<T>, CopyableSpliterator<T>{

    private final Iterable<T> source;

    Spliterator<T> active;

    public IteratableSpliterator(final Iterable<T> source) {
        this.source = source;
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {

        if(active==null)
            active = source.spliterator();


        active.forEachRemaining(action);

    }

    @Override
    public Spliterator<T> trySplit() {

        if(active==null)
            active=source.spliterator();

        return active.trySplit();

  }

    @Override
    public long estimateSize() {

        if(active==null)
            active=source.spliterator();

        return active.estimateSize();

    }

    @Override
    public int characteristics() {

      if(active==null)
        active=source.spliterator();

      return active.characteristics();

    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {

        if(active==null)
            active=source.spliterator();

        return active.tryAdvance(action);

    }

    @Override
    public Spliterator<T> copy() {
        return new IteratableSpliterator<>(source);
    }
}
