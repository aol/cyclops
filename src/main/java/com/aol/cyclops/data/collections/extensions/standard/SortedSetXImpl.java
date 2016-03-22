package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX.LazyCollection;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class SortedSetXImpl<T> implements SortedSetX<T> {
	
    private final LazyCollection<T,SortedSet<T>> lazy;
	@Getter
	private final Collector<T,?,SortedSet<T>> collector;

    public SortedSetXImpl(SortedSet<T> SortedSet, Collector<T, ?, SortedSet<T>> collector) {
        this.lazy = new LazyCollection<>(SortedSet, null, collector);
        this.collector = collector;
    }

    public SortedSetXImpl(SortedSet<T> SortedSet) {

        this.collector = SortedSetX.defaultCollector();
        this.lazy = new LazyCollection<T, SortedSet<T>>(SortedSet, null, collector);
    }

    private SortedSetXImpl(Stream<T> stream) {

        this.collector = SortedSetX.defaultCollector();
        this.lazy = new LazyCollection<>(null, stream, collector);
    }

    public SortedSetXImpl() {
        this.collector = SortedSetX.defaultCollector();
        this.lazy = new LazyCollection<>((SortedSet) this.collector.supplier().get(), null, collector);
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
	public boolean addAll(Collection<? extends T> c) {
		return getSet().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return getSet().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
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
	 * @return
	 * @see java.util.SortedSet#comparator()
	 */
	public Comparator<? super T> comparator() {
		return getSet().comparator();
	}
	/**
	 * @param fromElement
	 * @param toElement
	 * @return
	 * @see java.util.SortedSet#subSet(java.lang.Object, java.lang.Object)
	 */
	public SortedSetX<T> subSet(T fromElement, T toElement) {
		return from(getSet().subSet(fromElement, toElement));
	}
	/**
	 * @param toElement
	 * @return
	 * @see java.util.SortedSet#headSet(java.lang.Object)
	 */
	public SortedSetX<T> headSet(T toElement) {
		return from(getSet().headSet(toElement));
	}
	/**
	 * @param fromElement
	 * @return
	 * @see java.util.SortedSet#tailSet(java.lang.Object)
	 */
	public SortedSet<T> tailSet(T fromElement) {
		return from(getSet().tailSet(fromElement));
	}
	/**
	 * @return
	 * @see java.util.SortedSet#first()
	 */
	public T first() {
		return getSet().first();
	}
	/**
	 * @return
	 * @see java.util.SortedSet#last()
	 */
	public T last() {
		return getSet().last();
	}
	/**
	 * @return
	 * @see java.util.SortedSet#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getSet().spliterator();
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
	 * @see java.util.Collection#parallelStream()
	 */
	public Stream<T> parallelStream() {
		return getSet().parallelStream();
	}
    private SortedSet<T> getSet() {
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
    public <X> SortedSetX<X> stream(Stream<X> stream){
        return new SortedSetXImpl<X>(stream);
    }
	

}
