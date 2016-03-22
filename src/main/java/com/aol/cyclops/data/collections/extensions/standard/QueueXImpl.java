package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX.LazyCollection;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class QueueXImpl<T> implements QueueX<T> {
	
    private final LazyCollection<T,Queue<T>> lazy;
	@Getter
	private final Collector<T,?,Queue<T>> collector;
	
    public QueueXImpl(Queue<T> Queue, Collector<T, ?, Queue<T>> collector) {
        this.lazy = new LazyCollection<>(Queue, null, collector);
        this.collector = collector;
    }

    public QueueXImpl(Queue<T> Queue) {

        this.collector = QueueX.defaultCollector();
        this.lazy = new LazyCollection<T, Queue<T>>(Queue, null, collector);
    }

    private QueueXImpl(Stream<T> stream) {

        this.collector = QueueX.defaultCollector();
        this.lazy = new LazyCollection<>(null, stream, collector);
    }

    public QueueXImpl() {
        this.collector = QueueX.defaultCollector();
        this.lazy = new LazyCollection<>((Queue) this.collector.supplier().get(), null, collector);
    }

	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getQueue().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getQueue().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getQueue().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getQueue().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getQueue().equals(o);
	}



	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getQueue().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getQueue().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getQueue().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getQueue().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getQueue().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getQueue().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getQueue().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getQueue().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return getQueue().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return getQueue().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	public void clear() {
		getQueue().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getQueue().toString();
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
		return getQueue().removeIf(filter);
	}
	
	
	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public  Stream<T> parallelStream() {
		return getQueue().parallelStream();
	}
	
	/**
	 * @return
	 * @see java.util.List#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getQueue().spliterator();
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.Queue#offer(java.lang.Object)
	 */
	public boolean offer(T e) {
		return getQueue().offer(e);
	}
	/**
	 * @return
	 * @see java.util.Queue#remove()
	 */
	public T remove() {
		return getQueue().remove();
	}
	/**
	 * @return
	 * @see java.util.Queue#poll()
	 */
	public T poll() {
		return getQueue().poll();
	}
	/**
	 * @return
	 * @see java.util.Queue#element()
	 */
	public T element() {
		return getQueue().element();
	}
	/**
	 * @return
	 * @see java.util.Queue#peek()
	 */
	public T peek() {
		return getQueue().peek();
	}
    private Queue<T> getQueue() {
        return lazy.get();
    }
	
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return lazy.stream();
    }
    @Override
    public <X> QueueX<X> stream(Stream<X> stream){
        return new QueueXImpl<X>(stream);
    }

}
