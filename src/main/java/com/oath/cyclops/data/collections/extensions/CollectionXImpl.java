package com.oath.cyclops.data.collections.extensions;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.oath.cyclops.types.foldable.Evaluation;
import com.oath.cyclops.data.collections.extensions.standard.LazyCollectionX;
import cyclops.reactive.ReactiveSeq;
import cyclops.collections.mutable.ListX;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class CollectionXImpl<T> implements LazyCollectionX<T> {

    private final Collection<T> delegate;

    @Override
    public <R> CollectionX<R> unit(final R value) {
        return ListX.singleton(value);
    }

    @Override
    public <R> FluentCollectionX<R> unit(final Iterable<R> col) {
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
    public boolean containsValue(final Object o) {
        return delegate.contains(o);
    }
    @Override
    public boolean contains(Object o){
        return delegate.contains(o);
    }

    @Override
    public boolean isLazy() {
        return false;
    }

    @Override
    public boolean isEager() {
        return true;
    }

    @Override
    public Evaluation evaluation() {
        return Evaluation.EAGER;
    }

    @Override
    public CollectionX<T> lazy() {
        return this;
    }

    @Override
    public CollectionX<T> eager() {
        return this;
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

    @Override
    public boolean isMaterialized() {
        return true;
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
     * @see com.aol.cyclops2.collections.extensions.CollectionX#from(java.util.Collection)
     */
    @Override
    public <T1> CollectionX<T1> from(final Iterable<T1> c) {
        if (c instanceof CollectionX)
            return (CollectionX) c;
        return new CollectionXImpl(
                                   ListX.fromIterable(c));
    }


    /* (non-Javadoc)
     * @see com.aol.cyclops2.lambda.monads.IterableFunctor#unitIterable(java.util.Iterator)
     */
    @Override
    public <U> CollectionX<U> unitIterator(final Iterator<U> u) {
        return ListX.fromIterable(() -> u);
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops2.collections.extensions.standard.LazyCollectionX#fromStream(java.util.stream.Stream)
     */
    @Override
    public <X> LazyCollectionX<X> fromStream(final ReactiveSeq<X> stream) {
        return ListX.fromIterable(stream.collect(Collectors.toList()));
    }

    @Override
    public String toString() {
        return String.format("%s", delegate);
    }


}
