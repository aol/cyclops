package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.Wither;

@AllArgsConstructor
@EqualsAndHashCode(of = { "deque" })
public class DequeXImpl<T> implements DequeX<T> {

    private final Deque<T> deque;
    @Getter @Wither
    private final Collector<T, ?, Deque<T>> collector;

    public DequeXImpl(final Deque<T> list) {
        this.deque = list;
        this.collector = DequeX.defaultCollector();
    }

    public DequeXImpl() {
        this.collector = DequeX.defaultCollector();
        this.deque = (Deque) this.collector.supplier()
                                           .get();
    }

    @Override
    public void forEach(final Consumer<? super T> action) {
        deque.forEach(action);
    }

    @Override
    public Iterator<T> iterator() {
        return deque.iterator();
    }

    @Override
    public int size() {
        return deque.size();
    }

    @Override
    public boolean contains(final Object e) {
        return deque.contains(e);
    }

    @Override
    public boolean equals(final Object o) {
        if (o instanceof DequeXImpl)
            return deque.equals(((DequeXImpl) o).deque);

        return deque.equals(o);
    }

    @Override
    public boolean isEmpty() {
        return deque.isEmpty();
    }

    @Override
    public int hashCode() {
        return deque.hashCode();
    }

    @Override
    public Object[] toArray() {
        return deque.toArray();
    }

    @Override
    public boolean removeAll(final Collection<?> c) {
        return deque.removeAll(c);
    }

    @Override
    public <T> T[] toArray(final T[] a) {
        return deque.toArray(a);
    }

    @Override
    public boolean add(final T e) {
        return deque.add(e);
    }

    @Override
    public boolean remove(final Object o) {
        return deque.remove(o);
    }

    @Override
    public boolean containsAll(final Collection<?> c) {
        return deque.containsAll(c);
    }

    @Override
    public boolean addAll(final Collection<? extends T> c) {
        return deque.addAll(c);
    }

    @Override
    public boolean retainAll(final Collection<?> c) {
        return deque.retainAll(c);
    }

    @Override
    public void clear() {
        deque.clear();
    }

    @Override
    public String toString() {
        return deque.toString();
    }

    @Override
    public <R, A> R collect(final Collector<? super T, A, R> collector) {
        return stream().collect(collector);
    }

    @Override
    public long count() {
        return this.size();
    }

    @Override
    public boolean removeIf(final Predicate<? super T> filter) {
        return deque.removeIf(filter);
    }

    @Override
    public Stream<T> parallelStream() {
        return deque.parallelStream();
    }

    @Override
    public Spliterator<T> spliterator() {
        return deque.spliterator();
    }

    @Override
    public void addFirst(final T e) {
        deque.addFirst(e);
    }

    @Override
    public void addLast(final T e) {
        deque.addLast(e);
    }

    @Override
    public boolean offerFirst(final T e) {
        return deque.offerFirst(e);
    }

    @Override
    public boolean offerLast(final T e) {
        return deque.offerLast(e);
    }

    @Override
    public T removeFirst() {
        return deque.removeFirst();
    }

    @Override
    public T removeLast() {
        return deque.removeLast();
    }

    @Override
    public T pollFirst() {
        return deque.pollFirst();
    }

    @Override
    public T pollLast() {
        return deque.pollLast();
    }

    @Override
    public T getFirst() {
        return deque.getFirst();
    }

    @Override
    public T getLast() {
        return deque.getLast();
    }

    @Override
    public T peekFirst() {
        return deque.peekFirst();
    }

    @Override
    public T peekLast() {
        return deque.peekLast();
    }

    @Override
    public boolean removeFirstOccurrence(final Object o) {
        return deque.removeFirstOccurrence(o);
    }

    @Override
    public boolean removeLastOccurrence(final Object o) {
        return deque.removeLastOccurrence(o);
    }

    @Override
    public boolean offer(final T e) {
        return deque.offer(e);
    }

    @Override
    public T remove() {
        return deque.remove();
    }

    @Override
    public T poll() {
        return deque.poll();
    }

    @Override
    public T element() {
        return deque.element();
    }

    @Override
    public T peek() {
        return deque.peek();
    }

    @Override
    public void push(final T e) {
        deque.push(e);
    }

    @Override
    public T pop() {
        return deque.pop();
    }

    @Override
    public Iterator<T> descendingIterator() {
        return deque.descendingIterator();
    }

}
