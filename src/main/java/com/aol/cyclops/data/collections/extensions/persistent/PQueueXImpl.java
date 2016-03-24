package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Comparator;
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
import org.pcollections.PCollection;
import org.pcollections.PQueue;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.AbstractFluentCollectionX;
import com.aol.cyclops.data.collections.extensions.LazyFluentCollection;
import com.aol.cyclops.data.collections.extensions.persistent.PersistentCollectionX.PersistentLazyCollection;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class PQueueXImpl<T> extends AbstractFluentCollectionX<T> implements PQueueX<T> {
	
    private final LazyFluentCollection<T,PQueue<T>> lazy;
    public PQueueXImpl(PQueue<T> q){
        this.lazy = new PersistentLazyCollection<>(q,null,Reducers.toPQueue());
    }
    private PQueueXImpl(Stream<T> stream){
        this.lazy = new PersistentLazyCollection<>(null,stream,Reducers.toPQueue());
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

   
    @Override
    public <X> PQueueX<X> stream(Stream<X> stream){
        return new PQueueXImpl<X>(stream);
    }
      
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#stream()
     */
    @Override
    public ReactiveSeq<T> streamInternal() {
        return lazy.stream();
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(lazy.get().stream());
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public PQueueX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (PQueueX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#reverse()
     */
    @Override
    public PQueueX<T> reverse() {
       
        return(PQueueX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#filter(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> filter(Predicate<? super T> pred) {
       
        return (PQueueX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#map(java.util.function.Function)
     */
    @Override
    public <R> PQueueX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (PQueueX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> PQueueX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (PQueueX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#limit(long)
     */
    @Override
    public PQueueX<T> limit(long num) {
       return (PQueueX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#skip(long)
     */
    @Override
    public PQueueX<T> skip(long num) {
       return (PQueueX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#takeRight(int)
     */
    @Override
    public PQueueX<T> takeRight(int num) {
       return (PQueueX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#dropRight(int)
     */
    @Override
    public PQueueX<T> dropRight(int num) {
       return (PQueueX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> takeWhile(Predicate<? super T> p) {
       return (PQueueX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> dropWhile(Predicate<? super T> p) {
       return (PQueueX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> takeUntil(Predicate<? super T> p) {
       return (PQueueX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> dropUntil(Predicate<? super T> p) {
       return(PQueueX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> PQueueX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (PQueueX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#slice(long, long)
     */
    @Override
    public PQueueX<T> slice(long from, long to) {
       return (PQueueX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#grouped(int)
     */
    @Override
    public PQueueX<ListX<T>> grouped(int groupSize) {
       
        return (PQueueX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> PQueueX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (PQueueX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#grouped(java.util.function.Function)
     */
    @Override
    public <K> PQueueX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (PQueueX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#zip(java.lang.Iterable)
     */
    @Override
    public <U> PQueueX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (PQueueX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> PQueueX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (PQueueX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#sliding(int)
     */
    @Override
    public PQueueX<ListX<T>> sliding(int windowSize) {
       
        return (PQueueX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#sliding(int, int)
     */
    @Override
    public PQueueX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (PQueueX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public PQueueX<T> scanLeft(Monoid<T> monoid) {
       
        return (PQueueX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PQueueX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (PQueueX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public PQueueX<T> scanRight(Monoid<T> monoid) {
       
        return (PQueueX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PQueueX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (PQueueX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> PQueueX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (PQueueX<T>)super.sorted(function);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#plusLazy(java.lang.Object)
     */
    @Override
    public PQueueX<T> plusLazy(T e) {
       
        return (PQueueX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#plusAllLazy(java.util.Collection)
     */
    @Override
    public PQueueX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (PQueueX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#minusLazy(java.lang.Object)
     */
    @Override
    public PQueueX<T> minusLazy(Object e) {
       
        return (PQueueX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#minusAllLazy(java.util.Collection)
     */
    @Override
    public PQueueX<T> minusAllLazy(Collection<?> list) {
       
        return (PQueueX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(int)
     */
    @Override
    public PQueueX<T> cycle(int times) {
        
        return stream(this.stream().cycle(times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    public PQueueX<T> cycle(Monoid<T> m, int times) {
        
        return stream(this.stream().cycle(m,times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> cycleWhile(Predicate<? super T> predicate) {
        
        return stream(this.stream().cycleWhile(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> cycleUntil(Predicate<? super T> predicate) {
        
        return stream(this.stream().cycleUntil(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> PQueueX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (PQueueX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> PQueueX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (PQueueX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> PQueueX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (PQueueX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> PQueueX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (PQueueX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#zipWithIndex()
     */
    @Override
    public PQueueX<Tuple2<T, Long>> zipWithIndex() {
       
        return (PQueueX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#distinct()
     */
    @Override
    public PQueueX<T> distinct() {
       
        return (PQueueX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#sorted()
     */
    @Override
    public PQueueX<T> sorted() {
       
        return (PQueueX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#sorted(java.util.Comparator)
     */
    @Override
    public PQueueX<T> sorted(Comparator<? super T> c) {
       
        return (PQueueX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> skipWhile(Predicate<? super T> p) {
       
        return (PQueueX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> skipUntil(Predicate<? super T> p) {
       
        return (PQueueX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> limitWhile(Predicate<? super T> p) {
       
        return (PQueueX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> limitUntil(Predicate<? super T> p) {
       
        return (PQueueX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#intersperse(java.lang.Object)
     */
    @Override
    public PQueueX<T> intersperse(T value) {
       
        return (PQueueX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#shuffle()
     */
    @Override
    public PQueueX<T> shuffle() {
       
        return (PQueueX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#skipLast(int)
     */
    @Override
    public PQueueX<T> skipLast(int num) {
       
        return (PQueueX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#limitLast(int)
     */
    @Override
    public PQueueX<T> limitLast(int num) {
       
        return (PQueueX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#onEmpty(java.lang.Object)
     */
    @Override
    public PQueueX<T> onEmpty(T value) {
       
        return (PQueueX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public PQueueX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (PQueueX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> PQueueX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (PQueueX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#shuffle(java.util.Random)
     */
    @Override
    public PQueueX<T> shuffle(Random random) {
       
        return (PQueueX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#ofType(java.lang.Class)
     */
    @Override
    public <U> PQueueX<U> ofType(Class<U> type) {
       
        return (PQueueX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#filterNot(java.util.function.Predicate)
     */
    @Override
    public PQueueX<T> filterNot(Predicate<? super T> fn) {
       
        return (PQueueX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#notNull()
     */
    @Override
    public PQueueX<T> notNull() {
       
        return (PQueueX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#removeAll(java.util.stream.Stream)
     */
    @Override
    public PQueueX<T> removeAll(Stream<T> stream) {
       
        return (PQueueX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public PQueueX<T> removeAll(Seq<T> stream) {
       
        return (PQueueX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#removeAll(java.lang.Iterable)
     */
    @Override
    public PQueueX<T> removeAll(Iterable<T> it) {
       
        return (PQueueX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#removeAll(java.lang.Object[])
     */
    @Override
    public PQueueX<T> removeAll(T... values) {
       
        return (PQueueX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#retainAll(java.lang.Iterable)
     */
    @Override
    public PQueueX<T> retainAll(Iterable<T> it) {
       
        return (PQueueX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#retainAll(java.util.stream.Stream)
     */
    @Override
    public PQueueX<T> retainAll(Stream<T> stream) {
       
        return (PQueueX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public PQueueX<T> retainAll(Seq<T> stream) {
       
        return (PQueueX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#retainAll(java.lang.Object[])
     */
    @Override
    public PQueueX<T> retainAll(T... values) {
       
        return (PQueueX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#cast(java.lang.Class)
     */
    @Override
    public <U> PQueueX<U> cast(Class<U> type) {
       
        return (PQueueX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> PQueueX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (PQueueX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#permutations()
     */
    @Override
    public PQueueX<ReactiveSeq<T>> permutations() {
       
        return (PQueueX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#combinations(int)
     */
    @Override
    public PQueueX<ReactiveSeq<T>> combinations(int size) {
       
        return (PQueueX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#combinations()
     */
    @Override
    public PQueueX<ReactiveSeq<T>> combinations() {
       
        return (PQueueX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PQueueX<C> grouped(int size, Supplier<C> supplier) {
       
        return (PQueueX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public PQueueX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (PQueueX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public PQueueX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (PQueueX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PQueueX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PQueueX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PQueueX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PQueueX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PQueueX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public PQueueX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (PQueueX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** PQueueX methods **/

    /* Makes a defensive copy of this PQueueX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableSequenceX#with(int, java.lang.Object)
     */
    public PQueueX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> PQueueX<R> unit(Collection<R> col){
        return PQueueX.fromIterable(col);
    }
    @Override
    public  <R> PQueueX<R> unit(R value){
        return PQueueX.singleton(value);
    }
    @Override
    public <R> PQueueX<R> unitIterator(Iterator<R> it){
        return PQueueX.fromIterable(()->it);
    }
    
}
