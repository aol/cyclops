package com.aol.cyclops.data.collections.extensions.standard;

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
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;

import lombok.AllArgsConstructor;
import lombok.Getter;



public class ListXImpl<T> implements ListX<T> {
	
	private final LazyCollection<T,List<T>> lazy;
	@Getter
	private final Collector<T,?,List<T>> collector;
	
	public ListXImpl(List<T> list,Collector<T,?,List<T>> collector){
	    this.lazy = new LazyCollection<>(list,null,collector);
	    this.collector=  collector;
	}
	public ListXImpl(List<T> list){
		
		this.collector = ListX.defaultCollector();
		this.lazy = new LazyCollection<T,List<T>>(list,null,collector);
	}
	private ListXImpl(Stream<T> stream){
        
        this.collector = ListX.defaultCollector();
        this.lazy = new LazyCollection<>(null,stream,collector);
    }
	public ListXImpl(){
		this.collector = ListX.defaultCollector();
		this.lazy = new LazyCollection<>((List)this.collector.supplier().get(),null,collector);
	}
	
	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getList().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getList().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getList().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getList().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		return getList().equals(o);
	}



	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getList().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getList().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getList().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getList().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getList().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getList().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getList().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getList().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return getList().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return getList().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	public void clear() {
		getList().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getList().toString();
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
		return getList().addAll(index, c);
	}
	/**
	 * @param operator
	 * @see java.util.List#replaceAll(java.util.function.UnaryOperator)
	 */
	public void replaceAll(UnaryOperator<T> operator) {
		getList().replaceAll(operator);
	}
	/**
	 * @param filter
	 * @return
	 * @see java.util.Collection#removeIf(java.util.function.Predicate)
	 */
	public  boolean removeIf(Predicate<? super T> filter) {
		return getList().removeIf(filter);
	}
	/**
	 * @param c
	 * @see java.util.List#sort(java.util.Comparator)
	 */
	public  void sort(Comparator<? super T> c) {
		getList().sort(c);
	}
	/**
	 * @param index
	 * @return
	 * @see java.util.List#get(int)
	 */
	public T get(int index) {
		return getList().get(index);
	}
	/**
	 * @param index
	 * @param element
	 * @return
	 * @see java.util.List#set(int, java.lang.Object)
	 */
	public T set(int index, T element) {
		return getList().set(index, element);
	}
	/**
	 * @param index
	 * @param element
	 * @see java.util.List#add(int, java.lang.Object)
	 */
	public void add(int index, T element) {
		getList().add(index, element);
	}
	
	/**
	 * @param index
	 * @return
	 * @see java.util.List#remove(int)
	 */
	public T remove(int index) {
		return getList().remove(index);
	}
	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public  Stream<T> parallelStream() {
		return getList().parallelStream();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.List#indexOf(java.lang.Object)
	 */
	public int indexOf(Object o) {
		return getList().indexOf(o);
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.List#lastIndexOf(java.lang.Object)
	 */
	public int lastIndexOf(Object o) {
		return getList().lastIndexOf(o);
	}
	/**
	 * @return
	 * @see java.util.List#listIterator()
	 */
	public ListIterator<T> listIterator() {
		return getList().listIterator();
	}
	/**
	 * @param index
	 * @return
	 * @see java.util.List#listIterator(int)
	 */
	public ListIterator<T> listIterator(int index) {
		return getList().listIterator(index);
	}
	/**
	 * @param fromIndex
	 * @param toIndex
	 * @return
	 * @see java.util.List#subList(int, int)
	 */
	public ListX<T> subList(int fromIndex, int toIndex) {
		return new ListXImpl<T>(getList().subList(fromIndex, toIndex),getCollector());
	}
	/**
	 * @return
	 * @see java.util.List#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getList().spliterator();
	}
	/* (non-Javadoc)
	 * @see java.lang.Comparable#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(T o) {
		if(o instanceof List){
			List l = (List)o;
			if(this.size()==l.size()){
				Iterator i1 = iterator();
				Iterator i2 = l.iterator();
				if(i1.hasNext()){
					if(i2.hasNext()){
						int comp = Comparator.<Comparable>naturalOrder().compare((Comparable)i1.next(), (Comparable)i2.next());
						if(comp!=0)
							return comp;
					}
					return 1;
				}
				else{
					if(i2.hasNext())
						return -1;
					else
						return 0;
				}
			}
			return this.size() - ((List)o).size();
		}
		else
			return 1;
			
			
	}
    private List<T> getList() {
        return lazy.get();
    }
    @Override
	public <X> ListX<X> stream(Stream<X> stream){
	    return new ListXImpl<X>(stream);
	}
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return lazy.stream();
    }

}
