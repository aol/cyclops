package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class ListXImpl<T> implements ListX<T> {
	
	private final List<T> list;
	@Getter
	private final Collector<T,?,List<T>> collector;
	
	public ListXImpl(List<T> list){
		this.list = list;
		this.collector = Collectors.toList();
	}
	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		list.forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return list.iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return list.size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return list.contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return list.equals(o);
	}



	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return list.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return list.hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return list.toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return list.removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return list.toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return list.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return list.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
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
	 * @param index
	 * @param c
	 * @return
	 * @see java.util.List#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		return list.addAll(index, c);
	}
	/**
	 * @param operator
	 * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
	 */
	public void replaceAll(UnaryOperator<T> operator) {
		list.replaceAll(operator);
	}
	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public  boolean removeIf(Predicate<? super T> filter) {
		return list.removeIf(filter);
	}
	/**
	 * @param c
	 * @see java.util.List#sort(java.util.Comparator)
	 */
	public  void sort(Comparator<? super T> c) {
		list.sort(c);
	}
	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public T get(int index) {
		return list.get(index);
	}
	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		return list.set(index, element);
	}
	/**
	 * @param index
	 * @param element
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		list.add(index, element);
	}
	
	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public T remove(int index) {
		return list.remove(index);
	}
	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public  Stream<T> parallelStream() {
		return list.parallelStream();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return list.indexOf(o);
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return list.lastIndexOf(o);
	}
	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return list.listIterator();
	}
	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return list.listIterator(index);
	}
	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.List#subList(int, int)
	 */
	public List<T> subList(int fromIndex, int toIndex) {
		return list.subList(fromIndex, toIndex);
	}
	/**
	 * @return
	 * @see java.util.List#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return list.spliterator();
	}

	

}
