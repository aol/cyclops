package com.aol.cyclops2.data.collections.extensions.lazy;


import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.mutable.SetX;
import cyclops.collections.mutable.SortedSetX;
import cyclops.stream.ReactiveSeq;

import java.util.*;
import java.util.stream.Collector;

import static com.aol.cyclops2.types.foldable.Evaluation.LAZY;

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


    public LazySortedSetX(SortedSet<T> list, ReactiveSeq<T> seq, Collector<T, ?, SortedSet<T>> collector,Evaluation strict) {
        super(list, seq, collector,strict,r-> SortedSetX.defer(()->r.to().sortedSetX(LAZY).flatMap(i->i)));

    }
    public LazySortedSetX(SortedSet<T> list, Collector<T, ?, SortedSet<T>> collector,Evaluation strict) {
        super(list, null, collector,strict,r-> SortedSetX.defer(()->r.to().sortedSetX(LAZY).flatMap(i->i)));

    }

    public LazySortedSetX(ReactiveSeq<T> seq, Collector<T, ?, SortedSet<T>> collector,Evaluation strict) {
        super(null, seq, collector,strict,r-> SortedSetX.defer(()->r.to().sortedSetX(LAZY).flatMap(i->i)));

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
    public <T1> LazySortedSetX<T1> from(Collection<T1> c) {
        if(c instanceof Set)
            return new LazySortedSetX<T1>((SortedSet)c,null,(Collector)this.getCollectorInternal(), evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public <U> LazySortedSetX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazySortedSetX<R> unit(Collection<R> col) {
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
