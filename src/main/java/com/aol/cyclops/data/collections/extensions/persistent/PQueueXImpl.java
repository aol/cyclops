package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import cyclops.collections.immutable.PQueueX;
import org.pcollections.PCollection;
import org.pcollections.PQueue;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class PQueueXImpl<T> implements PQueueX<T> {

    private final PQueue<T> set;

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
    public PQueueX<T> plus(final T e) {
        return new PQueueXImpl<>(
                                 set.plus(e));
    }

    /**
     * @param e
     * @return
     * @see org.pcollections.MapPSet#minus(java.lang.Object)
     */
    @Override
    public PQueueX<T> minus(final Object e) {
        final PCollection<T> res = set.minus(e);
        if (res instanceof PQueue)
            return new PQueueXImpl<>(
                                     (PQueue<T>) res);
        else
            return PQueueX.fromCollection(res);
    }

    /**
     * @param list
     * @return
     * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
     */
    @Override
    public PQueueX<T> plusAll(final Collection<? extends T> list) {
        return new PQueueXImpl<>(
                                 set.plusAll(list));
    }

    /**
     * @param list
     * @return
     * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
     */
    @Override
    public PQueueX<T> minusAll(final Collection<?> list) {
        final PCollection<T> res = set.minusAll(list);
        if (res instanceof PQueue)
            return new PQueueXImpl<>(
                                     (PQueue<T>) res);
        else
            return PQueueX.fromCollection(res);
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
     * @return
     * @see org.pcollections.PQueue#minus()
     */
    @Override
    public PQueueX<T> minus() {
        return from(set.minus());
    }

    /**
     * @param o
     * @return
     * @deprecated
     * @see org.pcollections.PQueue#offer(java.lang.Object)
     */
    @Deprecated
    @Override
    public boolean offer(final T o) {
        return set.offer(o);
    }

    /**
     * @return
     * @deprecated
     * @see org.pcollections.PQueue#poll()
     */
    @Deprecated
    @Override
    public T poll() {
        return set.poll();
    }

    /**
     * @return
     * @deprecated
     * @see org.pcollections.PQueue#remove()
     */
    @Deprecated
    @Override
    public T remove() {
        return set.remove();
    }

    /**
     * @return
     * @see java.util.Queue#element()
     */
    @Override
    public T element() {
        return set.element();
    }

    /**
     * @return
     * @see java.util.Queue#peek()
     */
    @Override
    public T peek() {
        return set.peek();
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
