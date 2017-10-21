package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import com.aol.cyclops2.data.collections.extensions.api.PSet;
import com.aol.cyclops2.data.collections.extensions.standard.LazyCollectionX;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collectionx.immutable.PersistentSetX;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;


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
public class LazyPSetX<T> extends AbstractLazyPersistentCollection<T,PSet<T>> implements PersistentSetX<T> {

    public static final <T> Function<ReactiveSeq<PSet<T>>, PSet<T>> asyncSet() {
        return r -> {
            CompletablePersistentSetX<T> res = new CompletablePersistentSetX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asPersistentSetX();
        };
    }

    public LazyPSetX(PSet<T> list, ReactiveSeq<T> seq, Reducer<PSet<T>> reducer,Evaluation strict) {
        super(list, seq, reducer,strict,asyncSet());


    }

    //@Override
    public PersistentSetX<T> materialize() {
        get();
        return this;
    }


    @Override
    public PersistentSetX<T> type(Reducer<? extends PSet<T>> reducer) {
        return new LazyPSetX<T>(list,seq.get(),Reducer.narrow(reducer), evaluation());
    }

    //  @Override
    public <X> LazyPSetX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyPSetX<X>((PSet)getList(),ReactiveSeq.fromStream(stream),(Reducer)this.getCollectorInternal(), evaluation());
    }

    @Override
    public <T1> LazyPSetX<T1> from(Iterable<T1> c) {
        if(c instanceof PSet)
            return new LazyPSetX<T1>((PSet)c,null,(Reducer)this.getCollectorInternal(), evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    public <T1> LazyPSetX<T1> from(PSet<T1> c) {

        return new LazyPSetX<T1>((PSet)c,null,(Reducer)this.getCollectorInternal(), evaluation());

    }

    @Override
    public PersistentSetX<T> lazy() {
        return new LazyPSetX<T>(list,seq.get(),getCollectorInternal(), Evaluation.LAZY) ;
    }

    @Override
    public PersistentSetX<T> eager() {
        return new LazyPSetX<T>(list,seq.get(),getCollectorInternal(),Evaluation.EAGER) ;
    }
    @Override
    public PersistentSetX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public PersistentSetX<T> plusAll(Iterable<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public PersistentSetX<T> removeAll(Iterable<? extends T> list) {
        return from(get().removeAll(list));
    }


    @Override
    public PersistentSetX<T> removeValue(T remove) {
        return from(get().removeValue(remove));
    }



    @Override
    public <U> LazyPSetX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPSetX<R> unit(Iterable<R> col) {
        return from(col);
    }

    @Override
    public PersistentSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (PersistentSetX<T>)super.plusLoop(max,value);
    }

    @Override
    public PersistentSetX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (PersistentSetX<T>)super.plusLoop(supplier);
    }
}
