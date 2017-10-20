package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collectionx.immutable.PersistentQueueX;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;
import com.aol.cyclops2.data.collections.extensions.api.PQueue;

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
public class LazyPQueueX<T> extends AbstractLazyPersistentCollection<T,PQueue<T>> implements PersistentQueueX<T> {

    public static final <T> Function<ReactiveSeq<PQueue<T>>, PQueue<T>> asyncQueue() {
        return r -> {
            CompletablePersistentQueueX<T> res = new CompletablePersistentQueueX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asPersistentQueueX();
        };
    }

    public LazyPQueueX(PQueue<T> list, ReactiveSeq<T> seq, Reducer<PQueue<T>> reducer,Evaluation strict) {
        super(list, seq, reducer,strict,asyncQueue());
    }

    
    //@Override
    public PersistentQueueX<T> materialize() {
        get();
        return this;
    }


    @Override
    public PersistentQueueX<T> type(Reducer<? extends PQueue<T>> reducer) {
        return new LazyPQueueX<T>(list,seq.get(),Reducer.narrow(reducer), evaluation());
    }

    //  @Override
    public <X> LazyPQueueX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyPQueueX<X>((PQueue)getList(),ReactiveSeq.fromStream(stream),(Reducer)this.getCollectorInternal(), evaluation());
    }

    @Override
    public <T1> LazyPQueueX<T1> from(Iterable<T1> c) {
           return fromStream(ReactiveSeq.fromIterable(c));
    }

    public <T1> LazyPQueueX<T1> from(PQueue<T1> c) {
            return new LazyPQueueX<T1>((PQueue)c,null,(Reducer)this.getCollectorInternal(), evaluation());

    }

    @Override
    public PersistentQueueX<T> lazy() {
        return new LazyPQueueX<T>(list,seq.get(),getCollectorInternal(),Evaluation.LAZY) ;
    }

    @Override
    public PersistentQueueX<T> eager() {
        return new LazyPQueueX<T>(list,seq.get(),getCollectorInternal(),Evaluation.EAGER) ;
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
    public PersistentQueueX<T> plusAll(Iterable<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public PersistentQueueX<T> removeAll(Iterable<? extends T> list) {
        return from(get().removeAll(list));
    }
/**
    @Override
    public boolean offer(T o) {
        return getValue().offer(o);
    }

    @Override
    public T poll() {
        return getValue().poll();
    }

    @Override
    public T element() {
        return getValue().element();
    }

    @Override
    public T peek() {
        return getValue().peek();
    }

    @Override
    public T removeValue() {
        return getValue().removeValue();
    }

 **/
    @Override
    public PersistentQueueX<T> removeValue(T remove) {
        return from(get().removeValue(remove));
    }


    

    @Override
    public <U> LazyPQueueX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPQueueX<R> unit(Iterable<R> col) {
        return from(col);
    }

    @Override
    public PersistentQueueX<T> plusLoop(int max, IntFunction<T> value) {
        return (PersistentQueueX<T>)super.plusLoop(max,value);
    }

    @Override
    public PersistentQueueX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (PersistentQueueX<T>)super.plusLoop(supplier);
    }

    @Override
    public Option<T> get(int index) {
        return get().get(index);
    }

    @Override
    public T getOrElse(int index, T alt) {
        return get().getOrElse(index,alt);
    }

    @Override
    public T getOrElseGet(int index, Supplier<? extends T> alt) {
        return get().getOrElseGet(index,alt);
    }
}
