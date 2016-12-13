package com.aol.cyclops.internal.stream.spliterators;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class SinkSpliterator<T> implements Spliterator<T> {

    Spliterator<T> s;
    List<T> list;
    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        action.accept(t);
        return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        s.forEachRemaining(t->{
            list.add(t);
        });
        return null;
    }

    @Override
    public long estimateSize() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int characteristics() {
        // TODO Auto-generated method stub
        return 0;
    }

}
