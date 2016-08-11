package com.aol.cyclops.internal.react.stream;

import java.util.Iterator;

import com.aol.cyclops.data.async.Queue;
import com.aol.cyclops.react.async.subscription.Continueable;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CloseableIterator<T> implements Iterator<T> {

    private final Iterator<T> iterator;
    private final Continueable subscription;
    private final Queue queue;

    public boolean hasNext() {
        if (!iterator.hasNext())
            close();
        return iterator.hasNext();
    }

    public void close() {
        subscription.closeAll(queue);
    }

    public T next() {
        T next = iterator.next();
        return next;
    }

}
