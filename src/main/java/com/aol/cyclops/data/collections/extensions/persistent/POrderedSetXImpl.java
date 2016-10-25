package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.pcollections.POrderedSet;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class POrderedSetXImpl<T> implements POrderedSetX<T> {

    private final POrderedSet<T> set;

    /**
     * @param action
     * @see java.lang.Iterable#forEach(java.util.function.Consumer)
     */
    @Override
    public void forEach(final Consumer<? super T> action) {
        set.forEach(action);
    }

    /**
     * @return
     * @see org.pcollections.MapPSet#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return set.iterator();
    }

    /**
     * @return
     * @see org.pcollections.MapPSet#size()
     */
    @Override
    public int size() {
        return set.size();
    }

    /**
     * @param e
     * @return
     * @see org.pcollections.MapPSet#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object e) {
        return set.contains(e);
    }

    /**
     * @param o
     * @return
     * @see java.util.AbstractSet#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        return set.equals(o);
    }

    /**
     * @param e
     * @return
     * @see org.pcollections.MapPSet#plus(java.lang.Object)
     */
    @Override
    public POrderedSetX<T> plus(final T e) {
        return new POrderedSetXImpl<>(
                                      set.plus(e));
    }

    /**
     * @param e
     * @return
     * @see org.pcollections.MapPSet#minus(java.lang.Object)
     */
    @Override
    public POrderedSetX<T> minus(final Object e) {
        return new POrderedSetXImpl<>(
                                      set.minus(e));
    }

    /**
     * @param list
     * @return
     * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
     */
    @Override
    public POrderedSetX<T> plusAll(final Collection<? extends T> list) {
        return new POrderedSetXImpl<>(
                                      set.plusAll(list));
    }

    /**
     * @param list
     * @return
     * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
     */
    @Override
    public POrderedSetX<T> minusAll(final Collection<?> list) {
        return new POrderedSetXImpl<>(
                                      set.minusAll(list));
    }

    /**
     * @return
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return set.isEmpty();
    }

    /**
     * @return
     * @see java.util.AbstractSet#hashCode()
     */
    @Override
    public int hashCode() {
        return set.hashCode();
    }

    /**
     * @return
     * @see java.util.AbstractCollection#toArray()
     */
    @Override
    public Object[] toArray() {
        return set.toArray();
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractSet#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return set.removeAll(c);
    }

    /**
     * @param a
     * @return
     * @see java.util.AbstractCollection#toArray(java.lang.Object[])
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return set.toArray(a);
    }

    /**
     * @param e
     * @return
     * @see java.util.AbstractCollection#add(java.lang.Object)
     */
    @Override
    public boolean add(final T e) {
        return set.add(e);
    }

    /**
     * @param o
     * @return
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(final Object o) {
        return set.remove(o);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return set.containsAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#addAll(java.util.Collection)
     */
    @Override
    @Deprecated
    public boolean addAll(final Collection<? extends T> c) {
        return set.addAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#retainAll(java.util.Collection)
     */
    @Override
    @Deprecated
    public boolean retainAll(final Collection<?> c) {
        return set.retainAll(c);
    }

    /**
     * 
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    @Deprecated
    public void clear() {
        set.clear();
    }

    /**
     * @return
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return set.toString();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
     */
    @Override
    public <R, A> R collect(final Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#count()
     */
    @Override
    public long count() {
        return this.size();
    }

    /**
     * @param index
     * @return
     * @see org.pcollections.POrderedSet#get(int)
     */
    @Override
    public T get(final int index) {
        return set.get(index);
    }

    /**
     * @param o
     * @return
     * @see org.pcollections.POrderedSet#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(final Object o) {
        return set.indexOf(o);
    }

    /**
     * @param filter
     * @return
     * @see java.util.Collection#removeIf(java.util.function.Predicate)
     */
    @Override
    public boolean removeIf(final Predicate<? super T> filter) {
        return set.removeIf(filter);
    }

    /**
     * @return
     * @see java.util.Collection#spliterator()
     */
    @Override
    public Spliterator<T> spliterator() {
        return set.spliterator();
    }

    /**
     * @return
     * @see java.util.Collection#parallelStream()
     */
    @Override
    public Stream<T> parallelStream() {
        return set.parallelStream();
    }

}
