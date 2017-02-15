package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import cyclops.Reducers;
import cyclops.collections.immutable.POrderedSetX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.pcollections.POrderedSet;

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
public class LazyPOrderedSetX<T> extends AbstractLazyPersistentCollection<T,POrderedSet<T>> implements POrderedSetX<T> {


    public LazyPOrderedSetX(POrderedSet<T> list, ReactiveSeq<T> seq) {
        super(list, seq, Reducers.toPOrderedSet());
        

    }
    public LazyPOrderedSetX(POrderedSet<T> list, ReactiveSeq<T> seq, Reducer<POrderedSet<T>> reducer) {
        super(list, seq, reducer);


    }
    public LazyPOrderedSetX(POrderedSet<T> list) {
        super(list, null, Reducers.toPOrderedSet());
        
    }

    public LazyPOrderedSetX(ReactiveSeq<T> seq) {
        super(null, seq, Reducers.toPOrderedSet());
       

    }

    
    
    //@Override
    public POrderedSetX<T> materialize() {
        get();
        return this;
    }

  


  //  @Override
    private <X> LazyPOrderedSetX<X> fromStream(Stream<X> stream) {

        return new LazyPOrderedSetX<X>((POrderedSet)getList(),ReactiveSeq.fromStream(stream));
    }

    @Override
    public <T1> LazyPOrderedSetX<T1> from(Collection<T1> c) {
        if(c instanceof POrderedSet)
            return new LazyPOrderedSetX<T1>((POrderedSet)c,null);
        return fromStream(ReactiveSeq.fromIterable(c));
    }

  
    @Override
    public POrderedSetX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public POrderedSetX<T> plusAll(Collection<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public POrderedSetX<T> minusAll(Collection<?> list) {
        return from(get().minusAll(list));
    }

    @Override
    public T get(int index) {
        return get().get(index);
    }

    @Override
    public int indexOf(Object o) {
        return get().indexOf(o);
    }


    @Override
    public POrderedSetX<T> minus(Object remove) {
        return from(get().minus(remove));
    }


    

    @Override
    public <U> LazyPOrderedSetX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPOrderedSetX<R> unit(Collection<R> col) {
        return from(col);
    }
    @Override
    public POrderedSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (POrderedSetX<T>)super.plusLoop(max,value);
    }

    @Override
    public POrderedSetX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (POrderedSetX<T>)super.plusLoop(supplier);
    }
    
}
