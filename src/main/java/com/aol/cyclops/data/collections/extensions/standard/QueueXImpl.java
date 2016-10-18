package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class QueueXImpl<T> implements QueueX<T> {

    private final Queue<T> list;
    @Getter
    private final Collector<T, ?, Queue<T>> collector;

    public QueueXImpl(final Queue<T> list) {
        this.list = list;

        this.collector = QueueX.defaultCollector();
    }

    public QueueXImpl() {
        this.collector = QueueX.defaultCollector();
        this.list = (Queue) this.collector.supplier()
                                          .get();
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        list.forEach(action);
    }

    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean contains(final Object e) {
        return list.contains(e);
    }

    @Override
    public boolean equals(final Object o) {
        return list.equals(o);
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public Object[] toArray() {
        return list.toArray();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return list.removeAll(c);
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(final T e) {
        return list.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return list.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return list.containsAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(final Collection<? extends T> c) {
        return list.addAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return list.retainAll(c);
    }

    /**
     * 
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    public void clear() {
        list.clear();
    }

    /**
     * @return
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return list.toString();
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
     * @param filter
     * @return
     * @see java.util.Collection#removeIf(java.util.function.Predicate)
     */
    @Override
    public boolean removeIf(final Predicate<? super T> filter) {
        return list.removeIf(filter);
    }

    /**
     * @return
     * @see java.util.Collection#parallelStream()
     */
    @Override
    public Stream<T> parallelStream() {
        return list.parallelStream();
    }

    /**
     * @return
     * @see java.util.List#spliterator()
     */
    @Override
    public Spliterator<T> spliterator() {
        return list.spliterator();
    }

    /**
     * @param e
     * @return
     * @see java.util.Queue#offer(java.lang.Object)
     */
    @Override
    public boolean offer(final T e) {
        return list.offer(e);
    }

    /**
     * @return
     * @see java.util.Queue#remove()
     */
    @Override
    public T remove() {
        return list.remove();
    }

    /**
     * @return
     * @see java.util.Queue#poll()
     */
    @Override
    public T poll() {
        return list.poll();
    }

    /**
     * @return
     * @see java.util.Queue#element()
     */
    @Override
    public T element() {
        return list.element();
    }

    /**
     * @return
     * @see java.util.Queue#peek()
     */
    @Override
    public T peek() {
        return list.peek();
    }

}
