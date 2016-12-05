package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

@AllArgsConstructor
public class SortedSetXImpl<T> implements SortedSetX<T> {

    private final SortedSet<T> set;
    @Getter @Wither
    private final Collector<T, ?, SortedSet<T>> collector;

    public SortedSetXImpl(final SortedSet<T> set) {
        this.set = set;
        this.collector = Collectors.toCollection(() -> new TreeSet<>());
    }

    public SortedSetXImpl() {
        this.collector = SortedSetX.defaultCollector();
        this.set = (SortedSet) this.collector.supplier()
                                             .get();
    }

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
    public boolean addAll(final Collection<? extends T> c) {
        return set.addAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(final Collection<?> c) {
        return set.retainAll(c);
    }

    /**
     * 
     * @see java.util.AbstractCollection#clear()
     */
    @Override
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
     * @see java.util.SortedSet#comparator()
     */
    @Override
    public Comparator<? super T> comparator() {
        return set.comparator();
    }

    /**
     * @param fromElement
     * @param toElement
     * @return
     * @see java.util.SortedSet#subSet(java.lang.Object, java.lang.Object)
     */
    @Override
    public SortedSetX<T> subSet(final T fromElement, final T toElement) {
        return from(set.subSet(fromElement, toElement));
    }

    /**
     * @param toElement
     * @return
     * @see java.util.SortedSet#headSet(java.lang.Object)
     */
    @Override
    public SortedSetX<T> headSet(final T toElement) {
        return from(set.headSet(toElement));
    }

    /**
     * @param fromElement
     * @return
     * @see java.util.SortedSet#tailSet(java.lang.Object)
     */
    @Override
    public SortedSet<T> tailSet(final T fromElement) {
        return from(set.tailSet(fromElement));
    }

    /**
     * @return
     * @see java.util.SortedSet#first()
     */
    @Override
    public T first() {
        return set.first();
    }

    /**
     * @return
     * @see java.util.SortedSet#last()
     */
    @Override
    public T last() {
        return set.last();
    }

    /**
     * @return
     * @see java.util.SortedSet#spliterator()
     */
    @Override
    public Spliterator<T> spliterator() {
        return set.spliterator();
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
     * @see java.util.Collection#parallelStream()
     */
    @Override
    public Stream<T> parallelStream() {
        return set.parallelStream();
    }

}
