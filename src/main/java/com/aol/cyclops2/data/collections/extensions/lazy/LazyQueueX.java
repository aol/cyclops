package com.aol.cyclops2.data.collections.extensions.lazy;


import cyclops.collections.mutable.QueueX;
import cyclops.stream.ReactiveSeq;

import java.util.*;
import java.util.stream.Collector;

/**
 * An extended List type {@see java.util.List}
 * Extended List operations execute lazily e.g.
 * <pre>
 * {@code
 *    QueueX<Integer> q = QueueX.of(1,2,3)
 *                                      .map(i->i*2);
 * }
 * </pre>
 * The map operation above is not executed immediately. It will only be executed when (if) the data inside the
 * queue is accessed. This allows maybe operations to be chained and executed more efficiently e.g.
 *
 * <pre>
 * {@code
 *    QueueX<Integer> q = QueueX.of(1,2,3)
 *                              .map(i->i*2);
 *                              .filter(i->i<5);
 * }
 * </pre>
 *
 * The operation above is more efficient than the equivalent operation with a ListX.
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public class LazyQueueX<T> extends AbstractLazyMutableCollection<T,Queue<T>> implements QueueX<T> {


    public LazyQueueX(Queue<T> list, ReactiveSeq<T> seq, Collector<T, ?, Queue<T>> collector) {
        super(list, seq, collector);

    }
    public LazyQueueX(Queue<T> list, Collector<T, ?, Queue<T>> collector) {
        super(list, null, collector);

    }

    public LazyQueueX(ReactiveSeq<T> seq, Collector<T, ?, Queue<T>> collector) {
        super(null, seq, collector);

    }

    @Override
    public LazyQueueX<T> withCollector(Collector<T, ?, Queue<T>> collector){
        return (LazyQueueX)new LazyQueueX<T>(this.getList(),this.getSeq().get(),collector);
    }
    //@Override
    public QueueX<T> materialize() {
        get();
        return this;
    }

    @Override
    public <T1> Collector<T1, ?, Queue<T1>> getCollector() {
        return (Collector)super.getCollectorInternal();
    }



    @Override
    public <X> LazyQueueX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyQueueX<X>((Deque)getList(),ReactiveSeq.fromStream(stream),(Collector)this.getCollectorInternal());
    }

    @Override
    public <T1> LazyQueueX<T1> from(Collection<T1> c) {
        if(c instanceof Queue)
            return new LazyQueueX<T1>((Queue)c,null,(Collector)this.getCollectorInternal());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <U> LazyQueueX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyQueueX<R> unit(Collection<R> col) {
        return from(col);
    }



    @Override
    public boolean offer(T t) {
        return get().offer(t);
    }

    @Override
    public T remove() {
        return get().remove();
    }

    @Override
    public T poll() {
        return get().poll();
    }

    @Override
    public T element() {
        return get().element();
    }

    @Override
    public T peek() {
        return get().peek();
    }


}
