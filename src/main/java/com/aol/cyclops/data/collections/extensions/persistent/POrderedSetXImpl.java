package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.pcollections.PBag;
import org.pcollections.POrderedSet;
import org.pcollections.PSet;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class POrderedSetXImpl<T> implements POrderedSetX<T> {
	
	private final POrderedSet<T> set;

	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		set.forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return set.iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return set.size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return set.contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return set.equals(o);
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#plus(java.lang.Object)
	 */
	public POrderedSetX<T> plus(T e) {
		return new POrderedSetXImpl<>(set.plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  POrderedSetX<T> minus(Object e) {
		return new POrderedSetXImpl<>(set.minus(e));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  POrderedSetX<T> plusAll(Collection<? extends T> list) {
		return  new POrderedSetXImpl<>(set.plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public POrderedSetX<T> minusAll(Collection<?> list) {
		return  new POrderedSetXImpl<>(set.minusAll(list));
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return set.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return set.hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return set.toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return set.removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return set.toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return set.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return set.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return set.containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		return set.addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		return set.retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	@Deprecated
	public void clear() {
		set.clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return set.toString();
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
	 * @param index
	 * @return
	 * @see org.pcollections.POrderedSet#get(int)
	 */
	public T get(int index) {
		return set.get(index);
	}

	/**
	 * @param o
	 * @return
	 * @see org.pcollections.POrderedSet#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return set.indexOf(o);
	}

	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public  boolean removeIf(Predicate<? super T> filter) {
		return set.removeIf(filter);
	}

	/**
	 * @return
	 * @see java.util.Collection#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return set.spliterator();
	}

	

	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return set.parallelStream();
	}

	

}
