package com.aol.cyclops2.data.collections.extensions.lazy.immutable;


import cyclops.Reducers;
import cyclops.collections.immutable.PVectorX;
import cyclops.stream.ReactiveSeq;
import org.pcollections.PVector;

import java.util.*;
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
public class LazyPVectorX<T> extends AbstractLazyPersistentCollection<T,PVector<T>> implements PVectorX<T> {


    public LazyPVectorX(PVector<T> list, ReactiveSeq<T> seq) {
        super(list, seq, Reducers.toPVector());


    }
    public LazyPVectorX(PVector<T> list) {
        super(list, null, Reducers.toPVector());

    }

    public LazyPVectorX(ReactiveSeq<T> seq) {
        super(null, seq, Reducers.toPVector());


    }

    
    
    //@Override
    public PVectorX<T> materialize() {
        get();
        return this;
    }

  


  //  @Override
    private <X> LazyPVectorX<X> fromStream(Stream<X> stream) {

        return new LazyPVectorX<X>((PVector)getList(),ReactiveSeq.fromStream(stream));
    }

    @Override
    public <T1> LazyPVectorX<T1> from(Collection<T1> c) {
        if(c instanceof PVector)
            return new LazyPVectorX<T1>((PVector)c,null);
        return fromStream(ReactiveSeq.fromIterable(c));
    }

   
   

    @Override
    public PVectorX<T> minusAll(Collection<?> list) {
        return from(get().minusAll(list));
    }

    @Override
    public PVectorX<T> minus(Object remove) {
        return from(get().minus(remove));
    }

    @Override
    public PVectorX<T> with(int i, T e) {
        return from(get().with(i,e));
    }

    @Override
    public PVectorX<T> plus(int i, T e) {
        return from(get().plus(i,e));
    }

    @Override
    public PVectorX<T> plus(T e) {
        return from(get().plus(e));
    }

    @Override
    public PVectorX<T> plusAll(Collection<? extends T> list) {
        return from(get().plusAll(list));
    }

    @Override
    public PVectorX<T> plusAll(int i, Collection<? extends T> list) {
        return from(get().plusAll(i,list));
    }

    @Override
    public PVectorX<T> minus(int i) {
        return from(get().minus(i));
    }

    @Override
    public PVectorX<T> subList(int start, int end) {
        return from(get().subList(start,end));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return get().addAll(index,c);
    }

    @Override
    public T get(int index) {
        return get().get(index);
    }

    @Override
    public T set(int index, T element) {
        return get().set(index,element);
    }

    @Override
    public void add(int index, T element) {
         get().add(index,element);
    }

    @Override
    public T remove(int index) {
        return get().remove(index);
    }

    @Override
    public int indexOf(Object o) {
        return get().indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return get().lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator() {
        return get().listIterator();
    }

    @Override
    public ListIterator<T> listIterator(int index) {
        return get().listIterator(index);
    }



    @Override
    public <U> LazyPVectorX<U> unitIterator(Iterator<U> it) {
        return fromStream(ReactiveSeq.fromIterator(it));
    }



    @Override
    public <R> LazyPVectorX<R> unit(Collection<R> col) {
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

}
