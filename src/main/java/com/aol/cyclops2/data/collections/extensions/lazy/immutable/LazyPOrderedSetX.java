package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import com.aol.cyclops2.types.persistent.PersistentSortedSet;

import java.util.Iterator;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * An extended List type {@see java.util.List}
 * Extended List operations execute lazily e.g.
 * <pre>
 * {@code
 *    StreamX<Integer> q = StreamX.of(1,2,3)
 *                                      .map(i->i*2);
 * }
 * </pre>
 * The map operation above is not executed immediately. It will only be executed when (if) the data inside the
 * queue is accessed. This allows lazy operations to be chained and executed more efficiently e.g.
 *
 * <pre>
 * {@code
 *    DequeX<Integer> q = DequeX.of(1,2,3)
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
public class LazyPOrderedSetX<T> extends AbstractLazyPersistentCollection<T,PersistentSortedSet<T>> implements OrderedSetX<T> {
    public static final <T> Function<ReactiveSeq<PersistentSortedSet<T>>, PersistentSortedSet<T>> asyncOrderedSet() {
        return r -> {
            CompletableOrderedSetX<T> res = new CompletableOrderedSetX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asOrderedSetX();
        };
    }

    public LazyPOrderedSetX(PersistentSortedSet<T> list, ReactiveSeq<T> seq, Reducer<PersistentSortedSet<T>,T> reducer, Evaluation strict) {
        super(list, seq, reducer,strict,asyncOrderedSet());


    }

    //@Override
    public OrderedSetX<T> materialize() {
        get();
        return this;
    }


    @Override
    public OrderedSetX<T> type(Reducer<? extends PersistentSortedSet<T>,T> reducer) {
        return new LazyPOrderedSetX<T>(list,seq.get(),Reducer.narrow(reducer), evaluation());
    }

    //  @Override
    public <X> LazyPOrderedSetX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyPOrderedSetX<X>((PersistentSortedSet)getList(),ReactiveSeq.fromStream(stream),(Reducer)this.getCollectorInternal(), evaluation());
    }

    @Override
    public <T1> LazyPOrderedSetX<T1> from(Iterable<T1> c) {
        if(c instanceof PersistentSortedSet)
            return new LazyPOrderedSetX<T1>((PersistentSortedSet)c,null,(Reducer)this.getCollectorInternal(), evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
    }
    public <T1> LazyPOrderedSetX<T1> from(PersistentSortedSet<T1> c) {
        return new LazyPOrderedSetX<T1>((PersistentSortedSet)c,null,(Reducer)this.getCollectorInternal(), evaluation());
    }
    @Override
    public OrderedSetX<T> lazy() {
        return new LazyPOrderedSetX<T>(list,seq.get(),getCollectorInternal(),Evaluation.LAZY) ;
    }

    @Override
    public OrderedSetX<T> eager() {
        return new LazyPOrderedSetX<T>(list,seq.get(),getCollectorInternal(),Evaluation.EAGER) ;
    }

  
    @Override
    public OrderedSetX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public OrderedSetX<T> plusAll(Iterable<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public OrderedSetX<T> removeAll(Iterable<? extends T> list) {
        return from(get().removeAll(list));
    }

    @Override
    public Option<T> get(int index) {
        return get().get(index);
    }
/**
    @Override
    public int indexOf(Object o) {
        return getValue().indexOf(o);
    }
**/

    @Override
    public OrderedSetX<T> removeValue(T remove) {
        return from(get().removeValue(remove));
    }


    

    @Override
    public <U> LazyPOrderedSetX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPOrderedSetX<R> unit(Iterable<R> col) {
        return from(col);
    }
    @Override
    public OrderedSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (OrderedSetX<T>)super.plusLoop(max,value);
    }

    @Override
    public OrderedSetX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (OrderedSetX<T>)super.plusLoop(supplier);
    }
    
}
