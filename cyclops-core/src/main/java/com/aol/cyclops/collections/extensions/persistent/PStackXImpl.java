package com.aol.cyclops.collections.extensions.persistent;

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

import org.pcollections.PStack;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Wither;


@AllArgsConstructor
public class PStackXImpl<T> implements PStackX<T> {
	@Wither
	private final PStack<T> stack;
	@Wither @Getter
	private final boolean efficientOps;

	public PStackX<T> efficientOpsOn(){
		return this.withEfficientOps(true);
	}
	public PStackX<T> efficientOpsOff(){
		return this.withEfficientOps(false);
	}
	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		stack.forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return stack.iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return stack.size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return stack.contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return stack.equals(o);
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#plus(java.lang.Object)
	 */
	public PStackX<T> plus(T e) {
		return this.withStack(stack.plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  PStackX<T> minus(Object e) {
		return  this.withStack(stack.minus(e));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  PStackX<T> plusAll(Collection<? extends T> list) {
		return   this.withStack(stack.plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public PStackX<T> minusAll(Collection<?> list) {
		return   this.withStack(stack.minusAll(list));
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return stack.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return stack.hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return stack.toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return stack.removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return stack.toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return stack.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return stack.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return stack.containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		return stack.addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		return stack.retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	@Deprecated
	public void clear() {
		stack.clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return stack.toString();
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
	 * @param i
	 * @param e
	 * @return
	 * @see org.pcollections.PStack#with(int, java.lang.Object)
	 */
	public PStackX<T> with(int i, T e) {
		return  this.withStack(stack.with(i, e));
	}

	/**
	 * @param i
	 * @param e
	 * @return
	 * @see org.pcollections.PStack#plus(int, java.lang.Object)
	 */
	public PStackX<T> plus(int i, T e) {
		return this.withStack(stack.plus(i, e));
	}

	/**
	 * @param i
	 * @param list
	 * @return
	 * @see org.pcollections.PStack#plusAll(int, java.util.Collection)
	 */
	public PStackX<T> plusAll(int i, Collection<? extends T> list) {
		return  this.withStack(stack.plusAll(i, list));
	}

	/**
	 * @param i
	 * @return
	 * @see org.pcollections.PStack#minus(int)
	 */
	public PStackX<T> minus(int i) {
		return  this.withStack(stack.minus(i));
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 * @see org.pcollections.PStack#subList(int, int)
	 */
	public PStackX<T> subList(int start, int end) {
		return  this.withStack(stack.subList(start, end));
	}

	/**
	 * @param start
	 * @return
	 * @see org.pcollections.PStack#subList(int)
	 */
	public PStackX<T> subList(int start) {
		return  this.withStack(stack.subList(start));
	}

	/**
	 * @param index
	 * @param c
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		return stack.addAll(index, c);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		return stack.set(index, element);
	}

	/**
	 * @param index
	 * @param element
	 * @deprecated
	 * @see org.pcollections.PSequence#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		stack.add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#remove(int)
	 */
	public T remove(int index) {
		return stack.remove(index);
	}

	/**
	 * @param operator
	 * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
	 */
	public  void replaceAll(UnaryOperator<T> operator) {
		stack.replaceAll(operator);
	}

	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		return stack.removeIf(filter);
	}

	/**
	 * @param c
	 * @see java.util.List#sort(java.util.Comparator)
	 */
	public void sort(Comparator<? super T> c) {
		stack.sort(c);
	}

	/**
	 * @return
	 * @see java.util.Collection#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return stack.spliterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public T get(int index) {
		return stack.get(index);
	}

	

	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return stack.parallelStream();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return stack.indexOf(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return stack.lastIndexOf(o);
	}

	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return stack.listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return stack.listIterator(index);
	}

	

}
