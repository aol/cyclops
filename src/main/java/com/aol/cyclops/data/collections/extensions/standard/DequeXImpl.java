package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
import java.util.Deque;
import java.util.Iterator;
import java.util.Random;
import java.util.Spliterator;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.AbstractFluentCollectionX;
import com.aol.cyclops.data.collections.extensions.LazyFluentCollection;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@AllArgsConstructor
@EqualsAndHashCode(of={"deque"})
public class DequeXImpl<T> extends AbstractFluentCollectionX<T> implements DequeX<T> {
	
    private final  LazyFluentCollection<T,Deque<T>> lazy;

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
    public ReactiveSeq<T> streamInternal() {
        return lazy.stream();
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(lazy.get().stream());
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public DequeX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (DequeX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#reverse()
     */
    @Override
    public DequeX<T> reverse() {
       
        return(DequeX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#filter(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> filter(Predicate<? super T> pred) {
       
        return (DequeX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#map(java.util.function.Function)
     */
    @Override
    public <R> DequeX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (DequeX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> DequeX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (DequeX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#limit(long)
     */
    @Override
    public DequeX<T> limit(long num) {
       return (DequeX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#skip(long)
     */
    @Override
    public DequeX<T> skip(long num) {
       return (DequeX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#takeRight(int)
     */
    @Override
    public DequeX<T> takeRight(int num) {
       return (DequeX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#dropRight(int)
     */
    @Override
    public DequeX<T> dropRight(int num) {
       return (DequeX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> takeWhile(Predicate<? super T> p) {
       return (DequeX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> dropWhile(Predicate<? super T> p) {
       return (DequeX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> takeUntil(Predicate<? super T> p) {
       return (DequeX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> dropUntil(Predicate<? super T> p) {
       return(DequeX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> DequeX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (DequeX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#slice(long, long)
     */
    @Override
    public DequeX<T> slice(long from, long to) {
       return (DequeX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#grouped(int)
     */
    @Override
    public DequeX<ListX<T>> grouped(int groupSize) {
       
        return (DequeX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> DequeX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (DequeX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#grouped(java.util.function.Function)
     */
    @Override
    public <K> DequeX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (DequeX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#zip(java.lang.Iterable)
     */
    @Override
    public <U> DequeX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (DequeX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> DequeX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (DequeX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#sliding(int)
     */
    @Override
    public DequeX<ListX<T>> sliding(int windowSize) {
       
        return (DequeX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#sliding(int, int)
     */
    @Override
    public DequeX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (DequeX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public DequeX<T> scanLeft(Monoid<T> monoid) {
       
        return (DequeX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> DequeX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (DequeX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public DequeX<T> scanRight(Monoid<T> monoid) {
       
        return (DequeX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> DequeX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (DequeX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> DequeX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (DequeX<T>)super.sorted(function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#plus(java.lang.Object)
     */
    @Override
    public DequeX<T> plus(T e) {
       
        return (DequeX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#plusAll(java.util.Collection)
     */
    @Override
    public DequeX<T> plusAll(Collection<? extends T> list) {
       
        return (DequeX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#minus(java.lang.Object)
     */
    @Override
    public DequeX<T> minus(Object e) {
       
        return (DequeX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#minusAll(java.util.Collection)
     */
    @Override
    public DequeX<T> minusAll(Collection<?> list) {
       
        return (DequeX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#plusLazy(java.lang.Object)
     */
    @Override
    public DequeX<T> plusLazy(T e) {
       
        return (DequeX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#plusAllLazy(java.util.Collection)
     */
    @Override
    public DequeX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (DequeX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#minusLazy(java.lang.Object)
     */
    @Override
    public DequeX<T> minusLazy(Object e) {
       
        return (DequeX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#minusAllLazy(java.util.Collection)
     */
    @Override
    public DequeX<T> minusAllLazy(Collection<?> list) {
       
        return (DequeX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#cycle(int)
     */
    @Override
    public DequeX<T> cycle(int times) {
       
        return (DequeX<T>)super.cycle(times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    public DequeX<T> cycle(Monoid<T> m, int times) {
       
        return (DequeX<T>)super.cycle(m, times);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> cycleWhile(Predicate<? super T> predicate) {
       
        return (DequeX<T>)super.cycleWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> cycleUntil(Predicate<? super T> predicate) {
       
        return (DequeX<T>)super.cycleUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> DequeX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (DequeX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> DequeX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (DequeX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> DequeX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (DequeX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> DequeX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (DequeX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#zipWithIndex()
     */
    @Override
    public DequeX<Tuple2<T, Long>> zipWithIndex() {
       
        return (DequeX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#distinct()
     */
    @Override
    public DequeX<T> distinct() {
       
        return (DequeX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#sorted()
     */
    @Override
    public DequeX<T> sorted() {
       
        return (DequeX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#sorted(java.util.Comparator)
     */
    @Override
    public DequeX<T> sorted(Comparator<? super T> c) {
       
        return (DequeX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> skipWhile(Predicate<? super T> p) {
       
        return (DequeX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> skipUntil(Predicate<? super T> p) {
       
        return (DequeX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> limitWhile(Predicate<? super T> p) {
       
        return (DequeX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> limitUntil(Predicate<? super T> p) {
       
        return (DequeX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#intersperse(java.lang.Object)
     */
    @Override
    public DequeX<T> intersperse(T value) {
       
        return (DequeX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#shuffle()
     */
    @Override
    public DequeX<T> shuffle() {
       
        return (DequeX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#skipLast(int)
     */
    @Override
    public DequeX<T> skipLast(int num) {
       
        return (DequeX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#limitLast(int)
     */
    @Override
    public DequeX<T> limitLast(int num) {
       
        return (DequeX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#onEmpty(java.lang.Object)
     */
    @Override
    public DequeX<T> onEmpty(T value) {
       
        return (DequeX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public DequeX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (DequeX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> DequeX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (DequeX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#shuffle(java.util.Random)
     */
    @Override
    public DequeX<T> shuffle(Random random) {
       
        return (DequeX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#ofType(java.lang.Class)
     */
    @Override
    public <U> DequeX<U> ofType(Class<U> type) {
       
        return (DequeX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#filterNot(java.util.function.Predicate)
     */
    @Override
    public DequeX<T> filterNot(Predicate<? super T> fn) {
       
        return (DequeX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#notNull()
     */
    @Override
    public DequeX<T> notNull() {
       
        return (DequeX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#removeAll(java.util.stream.Stream)
     */
    @Override
    public DequeX<T> removeAll(Stream<T> stream) {
       
        return (DequeX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public DequeX<T> removeAll(Seq<T> stream) {
       
        return (DequeX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#removeAll(java.lang.Iterable)
     */
    @Override
    public DequeX<T> removeAll(Iterable<T> it) {
       
        return (DequeX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#removeAll(java.lang.Object[])
     */
    @Override
    public DequeX<T> removeAll(T... values) {
       
        return (DequeX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#retainAll(java.lang.Iterable)
     */
    @Override
    public DequeX<T> retainAll(Iterable<T> it) {
       
        return (DequeX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#retainAll(java.util.stream.Stream)
     */
    @Override
    public DequeX<T> retainAll(Stream<T> stream) {
       
        return (DequeX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public DequeX<T> retainAll(Seq<T> stream) {
       
        return (DequeX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#retainAll(java.lang.Object[])
     */
    @Override
    public DequeX<T> retainAll(T... values) {
       
        return (DequeX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#cast(java.lang.Class)
     */
    @Override
    public <U> DequeX<U> cast(Class<U> type) {
       
        return (DequeX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> DequeX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (DequeX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#permutations()
     */
    @Override
    public DequeX<ReactiveSeq<T>> permutations() {
       
        return (DequeX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#combinations(int)
     */
    @Override
    public DequeX<ReactiveSeq<T>> combinations(int size) {
       
        return (DequeX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#combinations()
     */
    @Override
    public DequeX<ReactiveSeq<T>> combinations() {
       
        return (DequeX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> DequeX<C> grouped(int size, Supplier<C> supplier) {
       
        return (DequeX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public DequeX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (DequeX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public DequeX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (DequeX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> DequeX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (DequeX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> DequeX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (DequeX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.DequeX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public DequeX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (DequeX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** DequeX methods **/

    /* Makes a defensive copy of this DequeX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
     */
    public DequeX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> DequeX<R> unit(Collection<R> col){
        return DequeX.fromIterable(col);
    }
    @Override
    public  <R> DequeX<R> unit(R value){
        return DequeX.singleton(value);
    }
    @Override
    public <R> DequeX<R> unitIterator(Iterator<R> it){
        return DequeX.fromIterable(()->it);
    }
}
