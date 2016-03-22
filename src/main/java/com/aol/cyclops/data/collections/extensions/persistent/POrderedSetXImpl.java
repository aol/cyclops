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

import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.persistent.PersistentCollectionX.LazyCollection;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class POrderedSetXImpl<T> implements POrderedSetX<T> {
	
    private final LazyCollection<T,POrderedSet<T>> lazy;
	public POrderedSetXImpl(POrderedSet<T> set){
        this.lazy = new LazyCollection<>(set,null,Reducers.toPOrderedSet());
    }
	private POrderedSetXImpl(Stream<T> stream){
        this.lazy = new LazyCollection<>(null,stream,Reducers.toPOrderedSet());
    }
	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getSet().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getSet().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getSet().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getSet().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getSet().equals(o);
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#plus(java.lang.Object)
	 */
	public POrderedSetX<T> plus(T e) {
		return new POrderedSetXImpl<>(getSet().plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  POrderedSetX<T> minus(Object e) {
		return new POrderedSetXImpl<>(getSet().minus(e));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  POrderedSetX<T> plusAll(Collection<? extends T> list) {
		return  new POrderedSetXImpl<>(getSet().plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public POrderedSetX<T> minusAll(Collection<?> list) {
		return  new POrderedSetXImpl<>(getSet().minusAll(list));
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getSet().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getSet().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getSet().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getSet().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getSet().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getSet().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getSet().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getSet().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		return getSet().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		return getSet().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	@Deprecated
	public void clear() {
		getSet().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getSet().toString();
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
		return getSet().get(index);
	}

	/**
	 * @param o
	 * @return
	 * @see org.pcollections.POrderedSet#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return getSet().indexOf(o);
	}

	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public  boolean removeIf(Predicate<? super T> filter) {
		return getSet().removeIf(filter);
	}

	/**
	 * @return
	 * @see java.util.Collection#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getSet().spliterator();
	}

	

	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return getSet().parallelStream();
	}

    private POrderedSet<T> getSet() {
        return lazy.get();
    }
   
    public <X> POrderedSetX<X> stream(Stream<X> stream){
        return new POrderedSetXImpl<X>(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return lazy.stream();
    }
	

}
