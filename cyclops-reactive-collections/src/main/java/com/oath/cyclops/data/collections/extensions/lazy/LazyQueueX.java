package com.oath.cyclops.data.collections.extensions.lazy;


import com.oath.cyclops.types.foldable.Evaluation;
import cyclops.reactive.collections.mutable.QueueX;
import cyclops.reactive.ReactiveSeq;
import lombok.EqualsAndHashCode;

import java.util.*;
import java.util.function.Function;
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
 * @param <T> the type of elements held in this toX
 */
@EqualsAndHashCode(of = { "queue" })
public class LazyQueueX<T> extends AbstractLazyCollection<T,Queue<T>> implements QueueX<T> {


    public static final <T> Function<ReactiveSeq<Queue<T>>, Queue<T>> asyncQueue() {
        return r -> {
            CompletableQueueX<T> res = new CompletableQueueX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asQueueX();
        };
    }
    public LazyQueueX(Queue<T> list, ReactiveSeq<T> seq, Collector<T, ?, Queue<T>> collector,Evaluation strict) {
        super(list, seq, collector,strict,asyncQueue());

    }
    public LazyQueueX(Queue<T> list, Collector<T, ?, Queue<T>> collector,Evaluation strict) {
        super(list, null, collector,strict,asyncQueue());

    }

    public LazyQueueX(ReactiveSeq<T> seq, Collector<T, ?, Queue<T>> collector,Evaluation strict) {
        super(null, seq, collector,strict,asyncQueue());

    }

    @Override
    public QueueX<T> type(Collector<T, ?, Queue<T>> collector) {
        return withCollector(collector);
    }

    @Override
    public LazyQueueX<T> withCollector(Collector<T, ?, Queue<T>> collector){
        return (LazyQueueX)new LazyQueueX<T>(this.getList(),this.getSeq().get(),collector, evaluation());
    }
    @Override
    public QueueX<T> lazy() {
        return new LazyQueueX<T>(getList(),getSeq().get(),getCollectorInternal(), Evaluation.LAZY) ;
    }

    @Override
    public QueueX<T> eager() {
        return new LazyQueueX<T>(getList(),getSeq().get(),getCollectorInternal(),Evaluation.EAGER) ;
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

        return new LazyQueueX(getList(),ReactiveSeq.fromStream(stream),(Collector)this.getCollectorInternal(), evaluation());
    }

    @Override
    public <T1> LazyQueueX<T1> from(Iterable<T1> c) {
        if(c instanceof Queue)
            return new LazyQueueX<T1>((Queue)c,null,(Collector)this.getCollectorInternal(), evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <U> LazyQueueX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyQueueX<R> unit(Iterable<R> col) {
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
