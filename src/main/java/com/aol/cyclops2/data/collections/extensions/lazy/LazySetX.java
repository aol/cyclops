package com.aol.cyclops2.data.collections.extensions.lazy;


import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.mutable.SetX;
import cyclops.reactive.ReactiveSeq;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;
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
 * @param <T> the type of elements held in this toX
 */
public class LazySetX<T> extends AbstractLazyCollection<T,Set<T>> implements SetX<T> {

    public static final <T> Function<ReactiveSeq<Set<T>>, Set<T>> asyncSet() {
        return r -> {
            CompletableSetX<T> res = new CompletableSetX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asSetX();
        };
    }
    public LazySetX(Set<T> list, ReactiveSeq<T> seq, Collector<T, ?, Set<T>> collector,Evaluation strict) {
        super(list, seq, collector,strict,asyncSet());

    }
    public LazySetX(Set<T> list, Collector<T, ?, Set<T>> collector,Evaluation strict) {
        super(list, null, collector,strict,asyncSet());

    }

    public LazySetX(ReactiveSeq<T> seq, Collector<T, ?, Set<T>> collector,Evaluation strict) {
        super(null, seq, collector,strict,asyncSet());

    }
    @Override
    public SetX<T> lazy() {
        return new LazySetX<T>(getList(),getSeq().get(),getCollectorInternal(), Evaluation.LAZY) ;
    }

    @Override
    public SetX<T> eager() {
        return new LazySetX<T>(getList(),getSeq().get(),getCollectorInternal(),Evaluation.EAGER) ;
    }

    @Override
    public LazySetX<T> type(Collector<T, ?, Set<T>> collector){
        return (LazySetX)new LazySetX<T>(this.getList(),this.getSeq().get(),collector, evaluation());
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

        return new LazySetX<X>((Set)getList(),ReactiveSeq.fromStream(stream),(Collector)this.getCollectorInternal(), evaluation());
    }

    @Override
    public <T1> LazySetX<T1> from(Iterable<T1> c) {
        if(c instanceof Set)
            return new LazySetX<T1>((Set)c,null,(Collector)this.getCollectorInternal(), evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <U> LazySetX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazySetX<R> unit(Iterable<R> col) {
        return from(col);
    }


}
