package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import cyclops.collections.immutable.BagX;
import cyclops.companion.Reducers;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.pcollections.PBag;

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
public class LazyPBagX<T> extends AbstractLazyPersistentCollection<T,PBag<T>> implements BagX<T> {



    public LazyPBagX(PBag<T> list, ReactiveSeq<T> seq, Reducer<PBag<T>> reducer) {
        super(list, seq, reducer);
    }

    @Override
    public BagX<T> plusLoop(int max, IntFunction<T> value) {
        return (BagX<T>)super.plusLoop(max,value);
    }

    @Override
    public BagX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (BagX<T>)super.plusLoop(supplier);
    }
    
    
    //@Override
    public BagX<T> materialize() {
        get();
        return this;
    }

    public BagX<T> type(Reducer<? extends PBag<T>> reducer){
        Reducer<PBag<T>> narrow = Reducer.narrow(reducer);
        return new LazyPBagX<T>(list,seq.get(),narrow);
    }




    @Override
    public <X> LazyPBagX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyPBagX<X>((PBag)getList(),ReactiveSeq.fromStream(stream),(Reducer)this.getCollectorInternal());
    }

    @Override
    public <T1> LazyPBagX<T1> from(Collection<T1> c) {
        if(c instanceof PBag)
            return new LazyPBagX<T1>((PBag)c,null,(Reducer)this.getCollectorInternal());
        return fromStream(ReactiveSeq.fromIterable(c));
    }


    @Override
    public BagX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public BagX<T> plusAll(Collection<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public BagX<T> minusAll(Collection<?> list) {
        return from(get().minusAll(list));
    }


    @Override
    public BagX<T> minus(Object remove) {
        return from(get().minus(remove));
    }


    

    @Override
    public <U> LazyPBagX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPBagX<R> unit(Collection<R> col) {
        return from(col);
    }

    
}
