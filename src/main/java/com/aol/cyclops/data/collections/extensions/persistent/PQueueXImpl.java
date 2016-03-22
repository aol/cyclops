package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.pcollections.PCollection;
import org.pcollections.POrderedSet;
import org.pcollections.PQueue;
import org.pcollections.PSet;

import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.persistent.PersistentCollectionX.LazyCollection;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class PQueueXImpl<T> implements PQueueX<T> {
	
    private final LazyCollection<T,PQueue<T>> lazy;
    public PQueueXImpl(PQueue<T> q){
        this.lazy = new LazyCollection<>(q,null,Reducers.toPQueue());
    }
    private PQueueXImpl(Stream<T> stream){
        this.lazy = new LazyCollection<>(null,stream,Reducers.toPQueue());
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
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#plus(java.lang.Object)
	 */
	public PQueueX<T> plus(T e) {
		return new PQueueXImpl<>(getQueue().plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  PQueueX<T> minus(Object e) {
		PCollection<T> res = getQueue().minus(e);
		if(res instanceof PQueue)
			return  new PQueueXImpl<>((PQueue<T>)res);
		else
			return PQueueX.fromCollection(res);
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  PQueueX<T> plusAll(Collection<? extends T> list) {
		return  new PQueueXImpl<>(getQueue().plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public PQueueX<T> minusAll(Collection<?> list) {
		PCollection<T> res = getQueue().minusAll(list);
		if(res instanceof PQueue)
			return  new PQueueXImpl<>((PQueue<T>)res);
		else
			return PQueueX.fromCollection(res);
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
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		return getQueue().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		return getQueue().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	@Deprecated
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
	 * @return
	 * @see org.pcollections.PQueue#minus()
	 */
	public PQueue<T> minus() {
		return getQueue().minus();
	}

	/**
	 * @param o
	 * @return
	 * @deprecated
	 * @see org.pcollections.PQueue#offer(java.lang.Object)
	 */
	public boolean offer(T o) {
		return getQueue().offer(o);
	}

	/**
	 * @return
	 * @deprecated
	 * @see org.pcollections.PQueue#poll()
	 */
	public T poll() {
		return getQueue().poll();
	}

	/**
	 * @return
	 * @deprecated
	 * @see org.pcollections.PQueue#remove()
	 */
	public T remove() {
		return getQueue().remove();
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

	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		return getQueue().removeIf(filter);
	}

	/**
	 * @return
	 * @see java.util.Collection#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getQueue().spliterator();
	}


	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return getQueue().parallelStream();
	}

    private PQueue<T> getQueue() {
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
    public <X> PQueueX<X> stream(Stream<X> stream){
        return new PQueueXImpl<X>(stream);
    }

}
