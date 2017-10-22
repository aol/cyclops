package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import com.aol.cyclops2.types.persistent.PersistentList;
import com.aol.cyclops2.types.foldable.Evaluation;
import cyclops.collectionx.immutable.VectorX;
import cyclops.control.Option;
import cyclops.function.Reducer;
import cyclops.reactive.ReactiveSeq;

import java.util.*;
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
public class LazyPVectorX<T> extends AbstractLazyPersistentCollection<T,PersistentList<T>> implements VectorX<T> {
    public static final <T> Function<ReactiveSeq<PersistentList<T>>, PersistentList<T>> asyncVector() {
        return r -> {
            CompletableVectorX<T> res = new CompletableVectorX<>();
            r.forEachAsync(l -> res.complete(l));
            return res.asVectorX();
        };
    }


    public LazyPVectorX(PersistentList<T> list, ReactiveSeq<T> seq, Reducer<PersistentList<T>,T> reducer, Evaluation strict) {
        super(list, seq, reducer,strict,asyncVector());
    }


    
    
    //@Override
    public VectorX<T> materialize() {
        get();
        return this;
    }


    @Override
    public VectorX<T> type(Reducer<? extends PersistentList<T>,T> reducer) {
        return new LazyPVectorX<T>(list,seq.get(),Reducer.narrow(reducer), evaluation());
    }

    //  @Override
    public <X> LazyPVectorX<X> fromStream(ReactiveSeq<X> stream) {

        return new LazyPVectorX<X>((PersistentList)getList(),ReactiveSeq.fromStream(stream),(Reducer)this.getCollectorInternal(), evaluation());
    }

    @Override
    public <T1> LazyPVectorX<T1> from(Iterable<T1> c) {
        if(c instanceof PersistentList)
            return new LazyPVectorX<T1>((PersistentList)c,null,(Reducer)this.getCollectorInternal(), evaluation());
        return fromStream(ReactiveSeq.fromIterable(c));
    }

    public <T1> LazyPVectorX<T1> from(PersistentList<T1> c) {

            return new LazyPVectorX<T1>((PersistentList)c,null,(Reducer)this.getCollectorInternal(), evaluation());

    }


    @Override
    public VectorX<T> lazy() {
        return new LazyPVectorX<T>(list,seq.get(),getCollectorInternal(), Evaluation.LAZY) ;
    }

    @Override
    public VectorX<T> eager() {
        return new LazyPVectorX<T>(list,seq.get(),getCollectorInternal(),Evaluation.EAGER) ;
    }

    @Override
    public VectorX<T> removeAll(Iterable<? extends T> list) {
        return from(get().removeAll(list));
    }

    @Override
    public VectorX<T> removeValue(T remove) {
        return from(get().removeValue(remove));
    }


    @Override
    public VectorX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public VectorX<T> plusAll(Iterable<? extends T> list) {
        return from(get().plusAll(list));
    }

    @Override
    public VectorX<T> insertAt(int i, T e) {
        return from(get().insertAt(i,e));
    }

    /**
    @Override
    public VectorX<T> insertAt(int i, Iterable<? extends T> list) {
        return from(getValue().insertAt(i,list));
    }
**/
    @Override
    public VectorX<T> removeAt(int i) {
        return from(get().removeAt(i));
    }

    @Override
    public VectorX<T> updateAt(int i, T e) {
        return from(get().updateAt(i,e));
    }

    /**
    @Override
    public VectorX<T> subList(int start, int end) {
        return from(get().subList(start,end));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return getValue().addAll(index,c);
    }

    @Override
    public T getValue(int index) {
        return getValue().getValue(index);
    }

    @Override
    public T set(int index, T element) {
        return getValue().set(index,element);
    }

    @Override
    public void add(int index, T element) {
         getValue().add(index,element);
    }

    @Override
    public T removeValue(int index) {
        return getValue().removeValue(index);
    }

    @Override
    public int indexOf(Object o) {
        return getValue().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return getValue().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return getValue().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return getValue().listIterator(index);
    }
**/


    @Override
    public <U> LazyPVectorX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPVectorX<R> unit(Iterable<R> col) {
        return from(col);
    }

    @Override
    public int compareTo(final T o) {
        if (o instanceof List) {
            final List l = (List) o;
            if (this.size() == l.size()) {
                final Iterator i1 = iterator();
                final Iterator i2 = l.iterator();
                if (i1.hasNext()) {
                    if (i2.hasNext()) {
                        final int comp = Comparator.<Comparable> naturalOrder()
                                .compare((Comparable) i1.next(), (Comparable) i2.next());
                        if (comp != 0)
                            return comp;
                    }
                    return 1;
                } else {
                    if (i2.hasNext())
                        return -1;
                    else
                        return 0;
                }
            }
            return this.size() - ((List) o).size();
        } else
            return 1;

    }
    @Override
    public VectorX<T> insertAt(int i, Iterable<? extends T> list) {
        return from(get().insertAt(i,list));
    }
    @Override
    public VectorX<T> plusLoop(int max, IntFunction<T> value) {
        return (VectorX<T>)super.plusLoop(max,value);
    }

    @Override
    public VectorX<T> plusLoop(Supplier<Option<T>> supplier) {
        return (VectorX<T>)super.plusLoop(supplier);
    }

    @Override
    public Option<T> get(int index) {
        return get().get(index);
    }

    @Override
    public T getOrElse(int index, T value) {
        PersistentList<T> x = get();
        if(index<0 || index>=x.size())
            return value;
        return x.getOrElse(index,value);
    }

    @Override
    public T getOrElseGet(int index, Supplier<? extends T> supplier) {
        PersistentList<T> x = get();
        if(index <0 || index>=x.size())
            return supplier.get();
        return x.getOrElseGet(index,supplier);
    }
    @Override
    public boolean equals(Object o) {
        if(o instanceof List){
            return equalToIteration((List)o);
        }
        return super.equals(o);
    }
}
