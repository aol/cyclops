package com.aol.cyclops.collections.extensions.standard;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
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
public class DequeXImpl<T> implements DequeX<T> {
	
	private final Deque<T> deque;
	@Getter
	private final Collector<T,?,Deque<T>> collector;
	
	public DequeXImpl(Deque<T> list){
		this.deque = list;
		this.collector = Collectors.toCollection(()-> new ArrayDeque<>());
	}
	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		deque.forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return deque.iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return deque.size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return deque.contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return deque.equals(o);
	}



	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return deque.isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return deque.hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return deque.toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return deque.removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return deque.toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return deque.add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return deque.remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return deque.containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return deque.addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return deque.retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	public void clear() {
		deque.clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return deque.toString();
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
	public  boolean removeIf(Predicate<? super T> filter) {
		return deque.removeIf(filter);
	}

	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public  Stream<T> parallelStream() {
		return deque.parallelStream();
	}


	/**
	 * @return
	 * @see java.util.List#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return deque.spliterator();
	}
	/**
	 * @param e
	 * @see java.util.Deque#addFirst(java.lang.Object)
	 */
	public void addFirst(T e) {
		deque.addFirst(e);
	}
	/**
	 * @param e
	 * @see java.util.Deque#addLast(java.lang.Object)
	 */
	public void addLast(T e) {
		deque.addLast(e);
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.Deque#offerFirst(java.lang.Object)
	 */
	public boolean offerFirst(T e) {
		return deque.offerFirst(e);
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.Deque#offerLast(java.lang.Object)
	 */
	public boolean offerLast(T e) {
		return deque.offerLast(e);
	}
	/**
	 * @return
	 * @see java.util.Deque#removeFirst()
	 */
	public T removeFirst() {
		return deque.removeFirst();
	}
	/**
	 * @return
	 * @see java.util.Deque#removeLast()
	 */
	public T removeLast() {
		return deque.removeLast();
	}
	/**
	 * @return
	 * @see java.util.Deque#pollFirst()
	 */
	public T pollFirst() {
		return deque.pollFirst();
	}
	/**
	 * @return
	 * @see java.util.Deque#pollLast()
	 */
	public T pollLast() {
		return deque.pollLast();
	}
	/**
	 * @return
	 * @see java.util.Deque#getFirst()
	 */
	public T getFirst() {
		return deque.getFirst();
	}
	/**
	 * @return
	 * @see java.util.Deque#getLast()
	 */
	public T getLast() {
		return deque.getLast();
	}
	/**
	 * @return
	 * @see java.util.Deque#peekFirst()
	 */
	public T peekFirst() {
		return deque.peekFirst();
	}
	/**
	 * @return
	 * @see java.util.Deque#peekLast()
	 */
	public T peekLast() {
		return deque.peekLast();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.Deque#removeFirstOccurrence(java.lang.Object)
	 */
	public boolean removeFirstOccurrence(Object o) {
		return deque.removeFirstOccurrence(o);
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.Deque#removeLastOccurrence(java.lang.Object)
	 */
	public boolean removeLastOccurrence(Object o) {
		return deque.removeLastOccurrence(o);
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.Deque#offer(java.lang.Object)
	 */
	public boolean offer(T e) {
		return deque.offer(e);
	}
	/**
	 * @return
	 * @see java.util.Deque#remove()
	 */
	public T remove() {
		return deque.remove();
	}
	/**
	 * @return
	 * @see java.util.Deque#poll()
	 */
	public T poll() {
		return deque.poll();
	}
	/**
	 * @return
	 * @see java.util.Deque#element()
	 */
	public T element() {
		return deque.element();
	}
	/**
	 * @return
	 * @see java.util.Deque#peek()
	 */
	public T peek() {
		return deque.peek();
	}
	/**
	 * @param e
	 * @see java.util.Deque#push(java.lang.Object)
	 */
	public void push(T e) {
		deque.push(e);
	}
	/**
	 * @return
	 * @see java.util.Deque#pop()
	 */
	public T pop() {
		return deque.pop();
	}
	/**
	 * @return
	 * @see java.util.Deque#descendingIterator()
	 */
	public Iterator<T> descendingIterator() {
		return deque.descendingIterator();
	}
	
	

}
