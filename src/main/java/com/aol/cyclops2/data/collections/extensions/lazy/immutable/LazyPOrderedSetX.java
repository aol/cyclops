package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collections.immutable.BagX;
import cyclops.collections.immutable.OrderedSetX;
import cyclops.function.Reducer;
import cyclops.stream.ReactiveSeq;
import org.pcollections.POrderedSet;

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
 * @param <T> the type of elements held in this toX
 */
public class LazyPOrderedSetX<T> extends AbstractLazyPersistentCollection<T,POrderedSet<T>> implements OrderedSetX<T> {


    public LazyPOrderedSetX(POrderedSet<T> list, ReactiveSeq<T> seq, Reducer<POrderedSet<T>> reducer,Evaluation strict) {
        super(list, seq, reducer,strict);


    }

    //@Override
    public OrderedSetX<T> materialize() {
        get();
        return this;
    }


    @Override
    public OrderedSetX<T> type(Reducer<? extends POrderedSet<T>> reducer) {
        return new LazyPOrderedSetX<T>(list,seq.get(),Reducer.narrow(reducer), evaluation());
    }

    //  @Override
    public <X> LazyPOrderedSetX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyPOrderedSetX<X>((POrderedSet)getList(),ReactiveSeq.fromStream(stream),(Reducer)this.getCollectorInternal(), evaluation());
    }

    @Override
    public <T1> LazyPOrderedSetX<T1> from(Collection<T1> c) {
        if(c instanceof POrderedSet)
            return new LazyPOrderedSetX<T1>((POrderedSet)c,null,(Reducer)this.getCollectorInternal(), evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
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
    public OrderedSetX<T> plusAll(Collection<? extends T> list) {
        return from(get().plusAll(list));
    }


    @Override
    public OrderedSetX<T> minusAll(Collection<?> list) {
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
    public OrderedSetX<T> minus(Object remove) {
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
    public OrderedSetX<T> plusLoop(int max, IntFunction<T> value) {
        return (OrderedSetX<T>)super.plusLoop(max,value);
    }

    @Override
    public OrderedSetX<T> plusLoop(Supplier<Optional<T>> supplier) {
        return (OrderedSetX<T>)super.plusLoop(supplier);
    }
    
}
