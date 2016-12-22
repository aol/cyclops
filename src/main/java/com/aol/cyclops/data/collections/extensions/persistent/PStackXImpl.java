package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Stream;

import cyclops.collections.immutable.PStackX;
import org.pcollections.PStack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;

@AllArgsConstructor
public class PStackXImpl<T> implements PStackX<T> {
    @Wither
    private final PStack<T> stack;
    @Wither
    @Getter
    private final boolean efficientOps;

    @Override
    public PStackX<T> efficientOpsOn() {
        return this.withEfficientOps(true);
    }

    @Override
    public PStackX<T> efficientOpsOff() {
        return this.withEfficientOps(false);
    }

    /**
     * @param action
     * @see java.lang.Iterable#forEach(java.util.function.Consumer)
     */
    @Override
    public void forEach(final Consumer<? super T> action) {
        stack.forEach(action);
    }

    /**
     * @return
     * @see org.pcollections.MapPSet#iterator()
     */
    @Override
    public Iterator<T> iterator() {
        return stack.iterator();
    }

    /**
     * @return
     * @see org.pcollections.MapPSet#size()
     */
    @Override
    public int size() {
        return stack.size();
    }

    /**
     * @param e
     * @return
     * @see org.pcollections.MapPSet#contains(java.lang.Object)
     */
    @Override
    public boolean contains(final Object e) {
        return stack.contains(e);
    }

    /**
     * @param o
     * @return
     * @see java.util.AbstractSet#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object o) {
        return stack.equals(o);
    }

    /**
     * @param e
     * @return
     * @see org.pcollections.MapPSet#plus(java.lang.Object)
     */
    @Override
    public PStackX<T> plus(final T e) {
        return this.withStack(stack.plus(e));
    }

    /**
     * @param e
     * @return
     * @see org.pcollections.MapPSet#minus(java.lang.Object)
     */
    @Override
    public PStackX<T> minus(final Object e) {
        return this.withStack(stack.minus(e));
    }

    /**
     * @param list
     * @return
     * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
     */
    @Override
    public PStackX<T> plusAll(final Collection<? extends T> list) {
        return this.withStack(stack.plusAll(list));
    }

    /**
     * @param list
     * @return
     * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
     */
    @Override
    public PStackX<T> minusAll(final Collection<?> list) {
        return this.withStack(stack.minusAll(list));
    }

    /**
     * @return
     * @see java.util.AbstractCollection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
        return stack.isEmpty();
    }

    /**
     * @return
     * @see java.util.AbstractSet#hashCode()
     */
    @Override
    public int hashCode() {
        return stack.hashCode();
    }

    /**
     * @return
     * @see java.util.AbstractCollection#toArray()
     */
    @Override
    public Object[] toArray() {
        return stack.toArray();
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractSet#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(final Collection<?> c) {
        return stack.removeAll(c);
    }

    /**
     * @param a
     * @return
     * @see java.util.AbstractCollection#toArray(java.lang.Object[])
     */
    @Override
    public <T> T[] toArray(final T[] a) {
        return stack.toArray(a);
    }

    /**
     * @param e
     * @return
     * @see java.util.AbstractCollection#add(java.lang.Object)
     */
    @Override
    public boolean add(final T e) {
        return stack.add(e);
    }

    /**
     * @param o
     * @return
     * @see java.util.AbstractCollection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(final Object o) {
        return stack.remove(o);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(final Collection<?> c) {
        return stack.containsAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#addAll(java.util.Collection)
     */
    @Override
    @Deprecated
    public boolean addAll(final Collection<? extends T> c) {
        return stack.addAll(c);
    }

    /**
     * @param c
     * @return
     * @see java.util.AbstractCollection#retainAll(java.util.Collection)
     */
    @Override
    @Deprecated
    public boolean retainAll(final Collection<?> c) {
        return stack.retainAll(c);
    }

    /**
     * 
     * @see java.util.AbstractCollection#clear()
     */
    @Override
    @Deprecated
    public void clear() {
        stack.clear();
    }

    /**
     * @return
     * @see java.util.AbstractCollection#toString()
     */
    @Override
    public String toString() {
        return stack.toString();
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
     * @param i
     * @param e
     * @return
     * @see org.pcollections.PStack#with(int, java.lang.Object)
     */
    @Override
    public PStackX<T> with(final int i, final T e) {
        return this.withStack(stack.with(i, e));
    }

    /**
     * @param i
     * @param e
     * @return
     * @see org.pcollections.PStack#plus(int, java.lang.Object)
     */
    @Override
    public PStackX<T> plus(final int i, final T e) {
        return this.withStack(stack.plus(i, e));
    }

    /**
     * @param i
     * @param list
     * @return
     * @see org.pcollections.PStack#plusAll(int, java.util.Collection)
     */
    @Override
    public PStackX<T> plusAll(final int i, final Collection<? extends T> list) {
        return this.withStack(stack.plusAll(i, list));
    }

    /**
     * @param i
     * @return
     * @see org.pcollections.PStack#minus(int)
     */
    @Override
    public PStackX<T> minus(final int i) {
        return this.withStack(stack.minus(i));
    }

    /**
     * @param start
     * @param end
     * @return
     * @see org.pcollections.PStack#subList(int, int)
     */
    @Override
    public PStackX<T> subList(final int start, final int end) {
        return this.withStack(stack.subList(start, end));
    }

    /**
     * @param start
     * @return
     * @see org.pcollections.PStack#subList(int)
     */
    @Override
    public PStackX<T> subList(final int start) {
        return this.withStack(stack.subList(start));
    }

    /**
     * @param index
     * @param c
     * @return
     * @deprecated
     * @see org.pcollections.PSequence#addAll(int, java.util.Collection)
     */
    @Deprecated
    @Override
    public boolean addAll(final int index, final Collection<? extends T> c) {
        return stack.addAll(index, c);
    }

    /**
     * @param index
     * @param element
     * @return
     * @deprecated
     * @see org.pcollections.PSequence#set(int, java.lang.Object)
     */
    @Deprecated
    @Override
    public T set(final int index, final T element) {
        return stack.set(index, element);
    }

    /**
     * @param index
     * @param element
     * @deprecated
     * @see org.pcollections.PSequence#add(int, java.lang.Object)
     */
    @Deprecated
    @Override
    public void add(final int index, final T element) {
        stack.add(index, element);
    }

    /**
     * @param index
     * @return
     * @deprecated
     * @see org.pcollections.PSequence#remove(int)
     */
    @Deprecated
    @Override
    public T remove(final int index) {
        return stack.remove(index);
    }

    /**
     * @param operator
     * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
     */
    @Override
    public void replaceAll(final UnaryOperator<T> operator) {
        stack.replaceAll(operator);
    }

    /**
     * @param filter
     * @return
     * @see java.util.Collection#removeIf(java.util.function.Predicate)
     */
    @Override
    public boolean removeIf(final Predicate<? super T> filter) {
        return stack.removeIf(filter);
    }

    /**
     * @param c
     * @see java.util.List#sort(java.util.Comparator)
     */
    @Override
    public void sort(final Comparator<? super T> c) {
        stack.sort(c);
    }

    /**
     * @return
     * @see java.util.Collection#spliterator()
     */
    @Override
    public Spliterator<T> spliterator() {
        return stack.spliterator();
    }

    /**
     * @param index
     * @return
     * @see java.util.List#get(int)
     */
    @Override
    public T get(final int index) {
        return stack.get(index);
    }

    /**
     * @return
     * @see java.util.Collection#parallelStream()
     */
    @Override
    public Stream<T> parallelStream() {
        return stack.parallelStream();
    }

    /**
     * @param o
     * @return
     * @see java.util.List#indexOf(java.lang.Object)
     */
    @Override
    public int indexOf(final Object o) {
        return stack.indexOf(o);
    }

    /**
     * @param o
     * @return
     * @see java.util.List#lastIndexOf(java.lang.Object)
     */
    @Override
    public int lastIndexOf(final Object o) {
        return stack.lastIndexOf(o);
    }

    /**
     * @return
     * @see java.util.List#listIterator()
     */
    @Override
    public ListIterator<T> listIterator() {
        return stack.listIterator();
    }

    /**
     * @param index
     * @return
     * @see java.util.List#listIterator(int)
     */
    @Override
    public ListIterator<T> listIterator(final int index) {
        return stack.listIterator(index);
    }

}
