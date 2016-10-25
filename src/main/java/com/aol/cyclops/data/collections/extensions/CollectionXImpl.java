package com.aol.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX;
import com.aol.cyclops.types.IterableFunctor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CollectionXImpl<T> implements MutableCollectionX<T> {

    private final Collection<T> delegate;

    @Override
    public <R> CollectionX<R> unit(final R value) {
        return ListX.singleton(value);
    }

    @Override
    public <R> FluentCollectionX<R> unit(final Collection<R> col) {
        return ListX.fromIterable(col);
    }

    /**
     * @param action
     * @see java.lang.Iterable#forEach(java.util.function.Consumer)
     */
    @Override
    public void forEach(final Consumer<? super T> action) {
        delegate.forEach(action);
    }

    /**
     * @return
     * @see java.util.Collection#size()
     */
    @Override
    public int size() {
        return delegate.size();
    }

    /**
     * @return
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    /**
     * @param o
     * @return
     * @see java.util.Collection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object o) {
        return delegate.contains(o);
    }

    /**
     * @return
     * @see java.util.Collection#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return delegate.iterator();
    }

    /**
     * @return
     * @see java.util.Collection#toArray()
     */
    @Override
    public Object[] toArray() {
        return delegate.toArray();
    }

    /**
     * @param a
     * @return
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return delegate.toArray(a);
    }

    /**
     * @param e
     * @return
     * @see java.util.Collection#add(java.lang.Object)
     */
    @Override
    public boolean add(final T e) {
        return delegate.add(e);
    }

    /**
     * @param o
     * @return
     * @see java.util.Collection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(final Object o) {
        return delegate.remove(o);
    }

    /**
     * @param c
     * @return
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return delegate.containsAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(final Collection<? extends T> c) {
        return delegate.addAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return delegate.removeAll(c);
    }

    /**
     * @param filter
     * @return
     * @see java.util.Collection#removeIf(java.util.function.Predicate)
     */
    @Override
    public boolean removeIf(final Predicate<? super T> filter) {
        return delegate.removeIf(filter);
    }

    /**
     * @param c
     * @return
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return delegate.retainAll(c);
    }

    /**
     * 
     * @see java.util.Collection#clear()
     */
    @Override
    public void clear() {
        delegate.clear();
    }

    /**
     * @param o
     * @return
     * @see java.util.Collection#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        return delegate.equals(o);
    }

    /**
     * @return
     * @see java.util.Collection#hashCode()
     */
    @Override
    public int hashCode() {
        return delegate.hashCode();
    }

    /**
     * @return
     * @see java.util.Collection#spliterator()
     */
    @Override
    public Spliterator<T> spliterator() {
        return delegate.spliterator();
    }

    /**
     * @return
     * @see java.util.Collection#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromIterable(this);
    }

    /**
     * @return
     * @see java.util.Collection#parallelStream()
     */
    @Override
    public Stream<T> parallelStream() {
        return delegate.parallelStream();
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.CollectionX#from(java.util.Collection)
     */
    @Override
    public <T1> CollectionX<T1> from(final Collection<T1> c) {
        if (c instanceof CollectionX)
            return (CollectionX) c;
        return new CollectionXImpl(
                                   c);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    public <U> IterableFunctor<U> unitIterator(final Iterator<U> u) {
        return ListX.fromIterable(() -> u);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#fromStream(java.util.stream.Stream)
     */
    @Override
    public <X> MutableCollectionX<X> fromStream(final Stream<X> stream) {
        return ListX.fromIterable(stream.collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return String.format("%s", delegate);
    }
}
