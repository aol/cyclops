package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Deque;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX.LazyCollection;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@AllArgsConstructor
@EqualsAndHashCode(of={"deque"})
public class DequeXImpl<T> implements DequeX<T> {
	
    private final LazyCollection<T,Deque<T>> lazy;

	@Getter
	private final Collector<T,?,Deque<T>> collector;
	
	   public DequeXImpl(Deque<T> Deque,Collector<T,?,Deque<T>> collector){
	        this.lazy = new LazyCollection<>(Deque,null,collector);
	        this.collector=  collector;
	    }
	    public DequeXImpl(Deque<T> Deque){
	        
	        this.collector = DequeX.defaultCollector();
	        this.lazy = new LazyCollection<T,Deque<T>>(Deque,null,collector);
	    }
	    private DequeXImpl(Stream<T> stream){
	        
	        this.collector = DequeX.defaultCollector();
	        this.lazy = new LazyCollection<>(null,stream,collector);
	    }
	    public DequeXImpl(){
	        this.collector = DequeX.defaultCollector();
	        this.lazy = new LazyCollection<>((Deque)this.collector.supplier().get(),null,collector);
	    }
	
	/**
	 * @param action
	 * @see java.lang.Iterable#forEach(java.util.function.Consumer)
	 */
	public void forEach(Consumer<? super T> action) {
		getDeque().forEach(action);
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#iterator()
	 */
	public Iterator<T> iterator() {
		return getDeque().iterator();
	}

	/**
	 * @return
	 * @see org.pcollections.MapPSet#size()
	 */
	public int size() {
		return getDeque().size();
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#contains(java.lang.Object)
	 */
	public boolean contains(Object e) {
		return getDeque().contains(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractSet#equals(java.lang.Object)
	 */
	public boolean equals(Object o) {
		if(o instanceof DequeXImpl)
			return getDeque().equals( ((DequeXImpl)o).getDeque());
			
		return getDeque().equals(o);
	}



	/**
	 * @return
	 * @see java.util.AbstractCollection#isEmpty()
	 */
	public boolean isEmpty() {
		return getDeque().isEmpty();
	}

	/**
	 * @return
	 * @see java.util.AbstractSet#hashCode()
	 */
	public int hashCode() {
		return getDeque().hashCode();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toArray()
	 */
	public Object[] toArray() {
		return getDeque().toArray();
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractSet#removeAll(java.util.Collection)
	 */
	public boolean removeAll(Collection<?> c) {
		return getDeque().removeAll(c);
	}

	/**
	 * @param a
	 * @return
	 * @see java.util.AbstractCollection#toArray(java.lang.Object[])
	 */
	public <T> T[] toArray(T[] a) {
		return getDeque().toArray(a);
	}

	/**
	 * @param e
	 * @return
	 * @see java.util.AbstractCollection#add(java.lang.Object)
	 */
	public boolean add(T e) {
		return getDeque().add(e);
	}

	/**
	 * @param o
	 * @return
	 * @see java.util.AbstractCollection#remove(java.lang.Object)
	 */
	public boolean remove(Object o) {
		return getDeque().remove(o);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#containsAll(java.util.Collection)
	 */
	public boolean containsAll(Collection<?> c) {
		return getDeque().containsAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#addAll(java.util.Collection)
	 */
	public boolean addAll(Collection<? extends T> c) {
		return getDeque().addAll(c);
	}

	/**
	 * @param c
	 * @return
	 * @see java.util.AbstractCollection#retainAll(java.util.Collection)
	 */
	public boolean retainAll(Collection<?> c) {
		return getDeque().retainAll(c);
	}

	/**
	 * 
	 * @see java.util.AbstractCollection#clear()
	 */
	public void clear() {
		getDeque().clear();
	}

	/**
	 * @return
	 * @see java.util.AbstractCollection#toString()
	 */
	public String toString() {
		return getDeque().toString();
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
		return getDeque().removeIf(filter);
	}

	/**
	 * @return
	 * @see java.util.Collection#parallelStream()
	 */
	public  Stream<T> parallelStream() {
		return getDeque().parallelStream();
	}


	/**
	 * @return
	 * @see java.util.List#spliterator()
	 */
	public Spliterator<T> spliterator() {
		return getDeque().spliterator();
	}
	/**
	 * @param e
	 * @see java.util.Deque#addFirst(java.lang.Object)
	 */
	public void addFirst(T e) {
		getDeque().addFirst(e);
	}
	/**
	 * @param e
	 * @see java.util.Deque#addLast(java.lang.Object)
	 */
	public void addLast(T e) {
		getDeque().addLast(e);
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.Deque#offerFirst(java.lang.Object)
	 */
	public boolean offerFirst(T e) {
		return getDeque().offerFirst(e);
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.Deque#offerLast(java.lang.Object)
	 */
	public boolean offerLast(T e) {
		return getDeque().offerLast(e);
	}
	/**
	 * @return
	 * @see java.util.Deque#removeFirst()
	 */
	public T removeFirst() {
		return getDeque().removeFirst();
	}
	/**
	 * @return
	 * @see java.util.Deque#removeLast()
	 */
	public T removeLast() {
		return getDeque().removeLast();
	}
	/**
	 * @return
	 * @see java.util.Deque#pollFirst()
	 */
	public T pollFirst() {
		return getDeque().pollFirst();
	}
	/**
	 * @return
	 * @see java.util.Deque#pollLast()
	 */
	public T pollLast() {
		return getDeque().pollLast();
	}
	/**
	 * @return
	 * @see java.util.Deque#getFirst()
	 */
	public T getFirst() {
		return getDeque().getFirst();
	}
	/**
	 * @return
	 * @see java.util.Deque#getLast()
	 */
	public T getLast() {
		return getDeque().getLast();
	}
	/**
	 * @return
	 * @see java.util.Deque#peekFirst()
	 */
	public T peekFirst() {
		return getDeque().peekFirst();
	}
	/**
	 * @return
	 * @see java.util.Deque#peekLast()
	 */
	public T peekLast() {
		return getDeque().peekLast();
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.Deque#removeFirstOccurrence(java.lang.Object)
	 */
	public boolean removeFirstOccurrence(Object o) {
		return getDeque().removeFirstOccurrence(o);
	}
	/**
	 * @param o
	 * @return
	 * @see java.util.Deque#removeLastOccurrence(java.lang.Object)
	 */
	public boolean removeLastOccurrence(Object o) {
		return getDeque().removeLastOccurrence(o);
	}
	/**
	 * @param e
	 * @return
	 * @see java.util.Deque#offer(java.lang.Object)
	 */
	public boolean offer(T e) {
		return getDeque().offer(e);
	}
	/**
	 * @return
	 * @see java.util.Deque#remove()
	 */
	public T remove() {
		return getDeque().remove();
	}
	/**
	 * @return
	 * @see java.util.Deque#poll()
	 */
	public T poll() {
		return getDeque().poll();
	}
	/**
	 * @return
	 * @see java.util.Deque#element()
	 */
	public T element() {
		return getDeque().element();
	}
	/**
	 * @return
	 * @see java.util.Deque#peek()
	 */
	public T peek() {
		return getDeque().peek();
	}
	/**
	 * @param e
	 * @see java.util.Deque#push(java.lang.Object)
	 */
	public void push(T e) {
		getDeque().push(e);
	}
	/**
	 * @return
	 * @see java.util.Deque#pop()
	 */
	public T pop() {
		return getDeque().pop();
	}
	/**
	 * @return
	 * @see java.util.Deque#descendingIterator()
	 */
	public Iterator<T> descendingIterator() {
		return getDeque().descendingIterator();
	}
    private Deque<T> getDeque() {
        return lazy.get();
    }
	
   
    @Override
    public <X> DequeX<X> stream(Stream<X> stream){
        return new DequeXImpl<X>(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return lazy.stream();
    }

}
