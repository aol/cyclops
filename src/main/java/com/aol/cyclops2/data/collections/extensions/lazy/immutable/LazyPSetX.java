package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import cyclops.Reducers;
import cyclops.collections.immutable.PSetX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.pcollections.PSet;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.function.IntFunction;
import java.util.function.Supplier;
import java.util.stream.Stream;

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
public class LazyPSetX<T> extends AbstractLazyPersistentCollection<T,PSet<T>> implements PSetX<T> {


    public LazyPSetX(PSet<T> list, ReactiveSeq<T> seq) {
        super(list, seq, Reducers.toPSet());
        

    }
    public LazyPSetX(PSet<T> list, ReactiveSeq<T> seq, Reducer<PSet<T>> reducer) {
        super(list, seq, reducer);


    }
    public LazyPSetX(PSet<T> list) {
        super(list, null, Reducers.toPSet());
        
    }

    public LazyPSetX(ReactiveSeq<T> seq) {
        super(null, seq, Reducers.toPSet());
       

    }

    
    
    //@Override
    public PSetX<T> materialize() {
        get();
        return this;
    }

  


  //  @Override
    private <X> LazyPSetX<X> fromStream(Stream<X> stream) {

        return new LazyPSetX<X>((PSet)getList(),ReactiveSeq.fromStream(stream));
    }

    @Override
    public <T1> LazyPSetX<T1> from(Collection<T1> c) {
        if(c instanceof PSet)
            return new LazyPSetX<T1>((PSet)c,null);
        return fromStream(ReactiveSeq.fromIterable(c));
    }


    @Override
    public PSetX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public PSetX<T> plusAll(Collection<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public PSetX<T> minusAll(Collection<?> list) {
        return from(get().minusAll(list));
    }


    @Override
    public PSetX<T> minus(Object remove) {
        return from(get().minus(remove));
    }


    

    @Override
    public <U> LazyPSetX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPSetX<R> unit(Collection<R> col) {
        return from(col);
    }

    @Override
    public PSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (PSetX<T>)super.plusLoop(max,value);
    }

    @Override
    public PSetX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (PSetX<T>)super.plusLoop(supplier);
    }
}
