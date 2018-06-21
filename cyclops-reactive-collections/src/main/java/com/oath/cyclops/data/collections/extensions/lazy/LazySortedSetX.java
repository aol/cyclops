package com.oath.cyclops.data.collections.extensions.lazy;


import com.oath.cyclops.types.foldable.Evaluation;
import cyclops.reactive.collections.mutable.SortedSetX;
import cyclops.reactive.ReactiveSeq;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * An extended Set type {@see java.util.List}
 * Extended Set operations execute lazily e.g.
 * <pre>
 * {@code
 *    SortedSetX<Integer> q = SortedSetX.of(1,2,3)
 *                                      .map(i->i*2);
 * }
 * </pre>
 * The map operation above is not executed immediately. It will only be executed when (if) the data inside the
 * queue is accessed. This allows lazy operations to be chained and executed more efficiently e.g.
 *
 * <pre>
 * {@code
 *    SortedSetX<Integer> q = SortedSetX.of(1,2,3)
 *                                      .map(i->i*2);
 *                                      .filter(i->i<5);
 * }
 * </pre>
 *
 * The operation above is more efficient than the equivalent operation with a ListX.
 *
 * @author johnmcclean
 *
 * @param <T> the type of elements held in this toX
 */
public class LazySortedSetX<T> extends AbstractLazyCollection<T,SortedSet<T>> implements SortedSetX<T> {

    public static final <T> Function<ReactiveSeq<SortedSet<T>>, SortedSet<T>> asyncSortedSet() {
        return r -> {
            CompletableSortedSetX<T> res = new CompletableSortedSetX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asSortedSetX();
        };
    }

    public LazySortedSetX(SortedSet<T> list, ReactiveSeq<T> seq, Collector<T, ?, SortedSet<T>> collector,Evaluation strict) {
        super(list, seq, collector,strict,asyncSortedSet());

    }
    public LazySortedSetX(SortedSet<T> list, Collector<T, ?, SortedSet<T>> collector,Evaluation strict) {
        super(list, null, collector,strict,asyncSortedSet());

    }

    public LazySortedSetX(ReactiveSeq<T> seq, Collector<T, ?, SortedSet<T>> collector,Evaluation strict) {
        super(null, seq, collector,strict,asyncSortedSet());

    }
    @Override
    public SortedSetX<T> lazy() {
        return new LazySortedSetX<T>(getList(),getSeq().get(),getCollectorInternal(), Evaluation.LAZY) ;
    }

    @Override
    public SortedSetX<T> eager() {
        return new LazySortedSetX<T>(getList(),getSeq().get(),getCollectorInternal(),Evaluation.EAGER) ;
    }
    @Override
    public LazySortedSetX<T> type(Collector<T, ?, SortedSet<T>> collector){
        return (LazySortedSetX)new LazySortedSetX<T>(this.getList(),this.getSeq().get(),collector, evaluation());
    }
    //@Override
    public SortedSetX<T> materialize() {
        get();
        return this;
    }

    @Override
    public <T1> Collector<T1, ?, SortedSet<T1>> getCollector() {
        return (Collector)super.getCollectorInternal();
    }



    @Override
    public <X> LazySortedSetX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazySortedSetX<X>((SortedSet)getList(),ReactiveSeq.fromStream(stream),(Collector)this.getCollectorInternal(), evaluation());
    }

    @Override
    public <T1> LazySortedSetX<T1> from(Iterable<T1> c) {
        if(c instanceof Set)
            return new LazySortedSetX<T1>((SortedSet)c,null,(Collector)this.getCollectorInternal(), evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <U> LazySortedSetX<U> unitIterable(Iterable<U> it) {
        return fromStream(ReactiveSeq.fromIterable(it));
    }



    @Override
    public <R> LazySortedSetX<R> unit(Iterable<R> col) {
        return from(col);
    }


    @Override
    public Comparator<? super T> comparator() {
        return get().comparator();
    }

    @Override
    public SortedSet<T> subSet(T fromElement, T toElement) {
        return get().subSet(fromElement,toElement);
    }

    @Override
    public SortedSet<T> headSet(T toElement) {
        return get().headSet(toElement);
    }

    @Override
    public SortedSet<T> tailSet(T fromElement) {
        return get().tailSet(fromElement);
    }

    @Override
    public T first() {
        return get().first();
    }

    @Override
    public T last() {
        return get().last();
    }
}
