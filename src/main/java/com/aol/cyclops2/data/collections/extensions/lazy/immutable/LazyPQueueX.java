package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import cyclops.collections.immutable.PersistentQueueX;
import cyclops.companion.Reducers;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.pcollections.PQueue;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;

/**
 * An extended List type {@see java.util.List}
 * Extended List operations execute lazily e.g.
 * <pre>
 * {@code
 *    LazyListX<Integer> q = LazyListX.of(1,2,3)
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
 * @param <T> the type of elements held in this collection
 */
public class LazyPQueueX<T> extends AbstractLazyPersistentCollection<T,PQueue<T>> implements PersistentQueueX<T> {


    public LazyPQueueX(PQueue<T> list, ReactiveSeq<T> seq, Reducer<PQueue<T>> reducer) {
        super(list, seq, reducer);
    }

    
    //@Override
    public PersistentQueueX<T> materialize() {
        get();
        return this;
    }


    @Override
    public PersistentQueueX<T> type(Reducer<? extends PQueue<T>> reducer) {
        return new LazyPQueueX<T>(list,seq.get(),Reducer.narrow(reducer));
    }

    //  @Override
    public <X> LazyPQueueX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyPQueueX<X>((PQueue)getList(),ReactiveSeq.fromStream(stream),(Reducer)this.getCollectorInternal());
    }

    @Override
    public <T1> LazyPQueueX<T1> from(Collection<T1> c) {
        if(c instanceof PQueue)
            return new LazyPQueueX<T1>((PQueue)c,null,(Reducer)this.getCollectorInternal());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    @Override
    public PersistentQueueX<T> minus() {
        return from(get().minus());
    }

    @Override
    public PersistentQueueX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public PersistentQueueX<T> plusAll(Collection<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public PersistentQueueX<T> minusAll(Collection<?> list) {
        return from(get().minusAll(list));
    }

    @Override
    public boolean offer(T o) {
        return get().offer(o);
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

    @Override
    public T remove() {
        return get().remove();
    }

    @Override
    public PersistentQueueX<T> minus(Object remove) {
        return from(get().minus(remove));
    }


    

    @Override
    public <U> LazyPQueueX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPQueueX<R> unit(Collection<R> col) {
        return from(col);
    }

    @Override
    public PersistentQueueX<T> plusLoop(int max, IntFunction<T> value) {
        return (PersistentQueueX<T>)super.plusLoop(max,value);
    }

    @Override
    public PersistentQueueX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (PersistentQueueX<T>)super.plusLoop(supplier);
    }
    
}
