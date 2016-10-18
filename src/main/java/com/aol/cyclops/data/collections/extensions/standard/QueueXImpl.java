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

    public QueueXImpl(Queue<T> list) {
        this.list = list;

        this.collector = QueueX.defaultCollector();
    }

    public QueueXImpl() {
        this.collector = QueueX.defaultCollector();
        this.list = (Queue) this.collector.supplier()
                                          .get();
    }

    public void forEach(Consumer<? super T> action) {
        list.forEach(action);
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public int size() {
        return list.size();
    }

    public boolean contains(Object e) {
        return list.contains(e);
    }

    public boolean equals(Object o) {
        return list.equals(o);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public int hashCode() {
        return list.hashCode();
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public boolean add(T e) {
        return list.add(e);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#addAll(java.util.Collection)
     */
    public boolean addAll(Collection<? extends T> c) {
        return list.addAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#retainAll(java.util.Collection)
     */
    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    /**
     * 
     * @see java.util.AbstractCollection#clear()
     */
    public void clear() {
        list.clear();
    }

    /**
     * @return
     * @see java.util.AbstractCollection#toString()
     */
    public String toString() {
        return list.toString();
    }

    /* (non-Javadoc)
     * @see org.jooq.lambda.Collectable#collect(java.util.stream.Collector)
     */
    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
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
    public boolean removeIf(Predicate<? super T> filter) {
        return list.removeIf(filter);
    }

    /**
     * @return
     * @see java.util.Collection#parallelStream()
     */
    public Stream<T> parallelStream() {
        return list.parallelStream();
    }

    /**
     * @return
     * @see java.util.List#spliterator()
     */
    public Spliterator<T> spliterator() {
        return list.spliterator();
    }

    /**
     * @param e
     * @return
     * @see java.util.Queue#offer(java.lang.Object)
     */
    public boolean offer(T e) {
        return list.offer(e);
    }

    /**
     * @return
     * @see java.util.Queue#remove()
     */
    public T remove() {
        return list.remove();
    }

    /**
     * @return
     * @see java.util.Queue#poll()
     */
    public T poll() {
        return list.poll();
    }

    /**
     * @return
     * @see java.util.Queue#element()
     */
    public T element() {
        return list.element();
    }

    /**
     * @return
     * @see java.util.Queue#peek()
     */
    public T peek() {
        return list.peek();
    }

}
