package com.aol.cyclops2.data.collections.extensions.lazy;


import cyclops.collections.mutable.SetX;
import cyclops.stream.ReactiveSeq;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collector;

/**
 * An extended Set type {@see java.util.List}
 * Extended Set operations execute lazily e.g.
 * <pre>
 * {@code
 *    SetX<Integer> q = SetX.of(1,2,3)
 *                                      .map(i->i*2);
 * }
 * </pre>
 * The map operation above is not executed immediately. It will only be executed when (if) the data inside the
 * queue is accessed. This allows lazy operations to be chained and executed more efficiently e.g.
 *
 * <pre>
 * {@code
 *    SetX<Integer> q = SetX.of(1,2,3)
 *                          .map(i->i*2);
 *                          .filter(i->i<5);
 * }
 * </pre>
 *
 * The operation above is more efficient than the equivalent operation with a ListX.
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this collection
 */
public class LazySetX<T> extends AbstractLazyCollection<T,Set<T>> implements SetX<T> {


    public LazySetX(Set<T> list, ReactiveSeq<T> seq, Collector<T, ?, Set<T>> collector) {
        super(list, seq, collector);

    }
    public LazySetX(Set<T> list, Collector<T, ?, Set<T>> collector) {
        super(list, null, collector);

    }

    public LazySetX(ReactiveSeq<T> seq, Collector<T, ?, Set<T>> collector) {
        super(null, seq, collector);

    }

    @Override
    public LazySetX<T> type(Collector<T, ?, Set<T>> collector){
        return (LazySetX)new LazySetX<T>(this.getList(),this.getSeq().get(),collector);
    }
    //@Override
    public SetX<T> materialize() {
        get();
        return this;
    }

    @Override
    public <T1> Collector<T1, ?, Set<T1>> getCollector() {
        return (Collector)super.getCollectorInternal();
    }



    @Override
    public <X> LazySetX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazySetX<X>((Set)getList(),ReactiveSeq.fromStream(stream),(Collector)this.getCollectorInternal());
    }

    @Override
    public <T1> LazySetX<T1> from(Collection<T1> c) {
        if(c instanceof Set)
            return new LazySetX<T1>((Set)c,null,(Collector)this.getCollectorInternal());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <U> LazySetX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazySetX<R> unit(Collection<R> col) {
        return from(col);
    }


}
