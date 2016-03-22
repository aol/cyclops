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

import org.pcollections.PQueue;
import org.pcollections.PVector;

import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.persistent.PersistentCollectionX.LazyCollection;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class PVectorXImpl<T> implements PVectorX<T> {
	
    private final LazyCollection<T,PVector<T>> lazy;
    public PVectorXImpl(PVector<T> q){
        this.lazy = new LazyCollection<>(q,null,Reducers.toPVector());
    }
    private PVectorXImpl(Stream<T> stream){
        this.lazy = new LazyCollection<>(null,stream,Reducers.toPVector());
    }

	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getVector().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getVector().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getVector().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getVector().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getVector().equals(o);
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#plus(java.lang.Object)
	 */
	public PVectorX<T> plus(T e) {
		return new PVectorXImpl<>(getVector().plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  PVectorX<T> minus(Object e) {
		return new PVectorXImpl<>(getVector().minus(e));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  PVectorX<T> plusAll(Collection<? extends T> list) {
		return  new PVectorXImpl<>(getVector().plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public PVectorX<T> minusAll(Collection<?> list) {
		return  new PVectorXImpl<>(getVector().minusAll(list));
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getVector().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getVector().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getVector().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getVector().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getVector().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getVector().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getVector().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getVector().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	@Deprecated
	public boolean addAll(Collection<? extends T> c) {
		return getVector().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	@Deprecated
	public boolean retainAll(Collection<?> c) {
		return getVector().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	@Deprecated
	public void clear() {
		getVector().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getVector().toString();
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
	public PVectorX<T> with(int i, T e) {
		return new PVectorXImpl<>(getVector().with(i, e));
	}

	/**
	 * @param i
	 * @param e
	 * @return
	 * @see org.pcollections.PStack#plus(int, java.lang.Object)
	 */
	public PVectorX<T> plus(int i, T e) {
		return new PVectorXImpl<>(getVector().plus(i, e));
	}

	/**
	 * @param i
	 * @param list
	 * @return
	 * @see org.pcollections.PStack#plusAll(int, java.util.Collection)
	 */
	public PVectorX<T> plusAll(int i, Collection<? extends T> list) {
		return new PVectorXImpl<>(getVector().plusAll(i, list));
	}

	/**
	 * @param i
	 * @return
	 * @see org.pcollections.PStack#minus(int)
	 */
	public PVectorX<T> minus(int i) {
		return new PVectorXImpl<>(getVector().minus(i));
	}

	/**
	 * @param start
	 * @param end
	 * @return
	 * @see org.pcollections.PStack#subList(int, int)
	 */
	public PVectorX<T> subList(int start, int end) {
		return new PVectorXImpl<>(getVector().subList(start, end));
	}

	

	/**
	 * @param index
	 * @param c
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#addAll(int, java.util.Collection)
	 */
	public boolean addAll(int index, Collection<? extends T> c) {
		return getVector().addAll(index, c);
	}

	/**
	 * @param index
	 * @param element
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		return getVector().set(index, element);
	}

	/**
	 * @param index
	 * @param element
	 * @deprecated
	 * @see org.pcollections.PSequence#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		getVector().add(index, element);
	}

	/**
	 * @param index
	 * @return
	 * @deprecated
	 * @see org.pcollections.PSequence#remove(int)
	 */
	public T remove(int index) {
		return getVector().remove(index);
	}

	/**
	 * @param operator
	 * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
	 */
	public  void replaceAll(UnaryOperator<T> operator) {
		getVector().replaceAll(operator);
	}

	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public boolean removeIf(Predicate<? super T> filter) {
		return getVector().removeIf(filter);
	}

	/**
	 * @param c
	 * @see java.util.List#sort(java.util.Comparator)
	 */
	public void sort(Comparator<? super T> c) {
		getVector().sort(c);
	}

	/**
	 * @return
	 * @see java.util.Collection#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getVector().spliterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public T get(int index) {
		return getVector().get(index);
	}

	

	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return getVector().parallelStream();
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return getVector().indexOf(o);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return getVector().lastIndexOf(o);
	}

	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return getVector().listIterator();
	}

	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return getVector().listIterator(index);
	}

    private PVector<T> getVector() {
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
    public <X> PVectorX<X> stream(Stream<X> stream){
        return new PVectorXImpl<X>(stream);
    }
}
