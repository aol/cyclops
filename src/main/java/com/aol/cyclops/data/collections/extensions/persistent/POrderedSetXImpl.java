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
import org.pcollections.POrderedSet;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.AbstractFluentCollectionX;
import com.aol.cyclops.data.collections.extensions.LazyFluentCollection;
import com.aol.cyclops.data.collections.extensions.persistent.PersistentCollectionX.PersistentLazyCollection;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.ListXImpl;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class POrderedSetXImpl<T> extends AbstractFluentCollectionX<T> implements POrderedSetX<T> {
	
    private final LazyFluentCollection<T,POrderedSet<T>> lazy;
	public POrderedSetXImpl(POrderedSet<T> set){
        this.lazy = new PersistentLazyCollection<>(set,null,Reducers.toPOrderedSet());
    }
	private POrderedSetXImpl(Stream<T> stream){
        this.lazy = new PersistentLazyCollection<>(null,stream,Reducers.toPOrderedSet());
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
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#stream()
     */
    @Override
    public ReactiveSeq<T> streamInternal() {
        return lazy.stream();
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(lazy.get().stream());
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public POrderedSetX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (POrderedSetX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#reverse()
     */
    @Override
    public POrderedSetX<T> reverse() {
       
        return(POrderedSetX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#filter(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> filter(Predicate<? super T> pred) {
       
        return (POrderedSetX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#map(java.util.function.Function)
     */
    @Override
    public <R> POrderedSetX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (POrderedSetX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> POrderedSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (POrderedSetX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#limit(long)
     */
    @Override
    public POrderedSetX<T> limit(long num) {
       return (POrderedSetX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#skip(long)
     */
    @Override
    public POrderedSetX<T> skip(long num) {
       return (POrderedSetX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#takeRight(int)
     */
    @Override
    public POrderedSetX<T> takeRight(int num) {
       return (POrderedSetX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#dropRight(int)
     */
    @Override
    public POrderedSetX<T> dropRight(int num) {
       return (POrderedSetX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> takeWhile(Predicate<? super T> p) {
       return (POrderedSetX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> dropWhile(Predicate<? super T> p) {
       return (POrderedSetX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> takeUntil(Predicate<? super T> p) {
       return (POrderedSetX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> dropUntil(Predicate<? super T> p) {
       return(POrderedSetX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> POrderedSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (POrderedSetX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#slice(long, long)
     */
    @Override
    public POrderedSetX<T> slice(long from, long to) {
       return (POrderedSetX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#grouped(int)
     */
    @Override
    public POrderedSetX<ListX<T>> grouped(int groupSize) {
       
        return (POrderedSetX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> POrderedSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (POrderedSetX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#grouped(java.util.function.Function)
     */
    @Override
    public <K> POrderedSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (POrderedSetX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#zip(java.lang.Iterable)
     */
    @Override
    public <U> POrderedSetX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (POrderedSetX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> POrderedSetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (POrderedSetX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#sliding(int)
     */
    @Override
    public POrderedSetX<ListX<T>> sliding(int windowSize) {
       
        return (POrderedSetX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#sliding(int, int)
     */
    @Override
    public POrderedSetX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (POrderedSetX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public POrderedSetX<T> scanLeft(Monoid<T> monoid) {
       
        return (POrderedSetX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> POrderedSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (POrderedSetX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public POrderedSetX<T> scanRight(Monoid<T> monoid) {
       
        return (POrderedSetX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> POrderedSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (POrderedSetX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> POrderedSetX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (POrderedSetX<T>)super.sorted(function);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#plusLazy(java.lang.Object)
     */
    @Override
    public POrderedSetX<T> plusLazy(T e) {
       
        return (POrderedSetX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#plusAllLazy(java.util.Collection)
     */
    @Override
    public POrderedSetX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (POrderedSetX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#minusLazy(java.lang.Object)
     */
    @Override
    public POrderedSetX<T> minusLazy(Object e) {
       
        return (POrderedSetX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#minusAllLazy(java.util.Collection)
     */
    @Override
    public POrderedSetX<T> minusAllLazy(Collection<?> list) {
       
        return (POrderedSetX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(int)
     */
    @Override
    public ListX<T> cycle(int times) {
        
        return new ListXImpl<T>(this.stream().cycle(times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    public ListX<T> cycle(Monoid<T> m, int times) {
        
        return new ListXImpl<T>(this.stream().cycle(m,times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<T> cycleWhile(Predicate<? super T> predicate) {
        
        return new ListXImpl<T>(this.stream().cycleWhile(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<T> cycleUntil(Predicate<? super T> predicate) {
        
        return new ListXImpl<T>(this.stream().cycleUntil(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> POrderedSetX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (POrderedSetX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> POrderedSetX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (POrderedSetX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> POrderedSetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (POrderedSetX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> POrderedSetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (POrderedSetX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#zipWithIndex()
     */
    @Override
    public POrderedSetX<Tuple2<T, Long>> zipWithIndex() {
       
        return (POrderedSetX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#distinct()
     */
    @Override
    public POrderedSetX<T> distinct() {
       
        return (POrderedSetX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#sorted()
     */
    @Override
    public POrderedSetX<T> sorted() {
       
        return (POrderedSetX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#sorted(java.util.Comparator)
     */
    @Override
    public POrderedSetX<T> sorted(Comparator<? super T> c) {
       
        return (POrderedSetX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> skipWhile(Predicate<? super T> p) {
       
        return (POrderedSetX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> skipUntil(Predicate<? super T> p) {
       
        return (POrderedSetX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> limitWhile(Predicate<? super T> p) {
       
        return (POrderedSetX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> limitUntil(Predicate<? super T> p) {
       
        return (POrderedSetX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#intersperse(java.lang.Object)
     */
    @Override
    public POrderedSetX<T> intersperse(T value) {
       
        return (POrderedSetX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#shuffle()
     */
    @Override
    public POrderedSetX<T> shuffle() {
       
        return (POrderedSetX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#skipLast(int)
     */
    @Override
    public POrderedSetX<T> skipLast(int num) {
       
        return (POrderedSetX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#limitLast(int)
     */
    @Override
    public POrderedSetX<T> limitLast(int num) {
       
        return (POrderedSetX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#onEmpty(java.lang.Object)
     */
    @Override
    public POrderedSetX<T> onEmpty(T value) {
       
        return (POrderedSetX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public POrderedSetX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (POrderedSetX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> POrderedSetX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (POrderedSetX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#shuffle(java.util.Random)
     */
    @Override
    public POrderedSetX<T> shuffle(Random random) {
       
        return (POrderedSetX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#ofType(java.lang.Class)
     */
    @Override
    public <U> POrderedSetX<U> ofType(Class<U> type) {
       
        return (POrderedSetX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#filterNot(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<T> filterNot(Predicate<? super T> fn) {
       
        return (POrderedSetX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#notNull()
     */
    @Override
    public POrderedSetX<T> notNull() {
       
        return (POrderedSetX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#removeAll(java.util.stream.Stream)
     */
    @Override
    public POrderedSetX<T> removeAll(Stream<T> stream) {
       
        return (POrderedSetX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public POrderedSetX<T> removeAll(Seq<T> stream) {
       
        return (POrderedSetX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#removeAll(java.lang.Iterable)
     */
    @Override
    public POrderedSetX<T> removeAll(Iterable<T> it) {
       
        return (POrderedSetX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#removeAll(java.lang.Object[])
     */
    @Override
    public POrderedSetX<T> removeAll(T... values) {
       
        return (POrderedSetX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#retainAll(java.lang.Iterable)
     */
    @Override
    public POrderedSetX<T> retainAll(Iterable<T> it) {
       
        return (POrderedSetX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#retainAll(java.util.stream.Stream)
     */
    @Override
    public POrderedSetX<T> retainAll(Stream<T> stream) {
       
        return (POrderedSetX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public POrderedSetX<T> retainAll(Seq<T> stream) {
       
        return (POrderedSetX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#retainAll(java.lang.Object[])
     */
    @Override
    public POrderedSetX<T> retainAll(T... values) {
       
        return (POrderedSetX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#cast(java.lang.Class)
     */
    @Override
    public <U> POrderedSetX<U> cast(Class<U> type) {
       
        return (POrderedSetX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> POrderedSetX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (POrderedSetX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#permutations()
     */
    @Override
    public POrderedSetX<ReactiveSeq<T>> permutations() {
       
        return (POrderedSetX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#combinations(int)
     */
    @Override
    public POrderedSetX<ReactiveSeq<T>> combinations(int size) {
       
        return (POrderedSetX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#combinations()
     */
    @Override
    public POrderedSetX<ReactiveSeq<T>> combinations() {
       
        return (POrderedSetX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> POrderedSetX<C> grouped(int size, Supplier<C> supplier) {
       
        return (POrderedSetX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (POrderedSetX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public POrderedSetX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (POrderedSetX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> POrderedSetX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (POrderedSetX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> POrderedSetX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (POrderedSetX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.POrderedSetX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public POrderedSetX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (POrderedSetX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** POrderedSetX methods **/

    /* Makes a defensive copy of this POrderedSetX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableSequenceX#with(int, java.lang.Object)
     */
    public POrderedSetX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> POrderedSetX<R> unit(Collection<R> col){
        return POrderedSetX.fromIterable(col);
    }
    @Override
    public  <R> POrderedSetX<R> unit(R value){
        return POrderedSetX.singleton(value);
    }
    @Override
    public <R> POrderedSetX<R> unitIterator(Iterator<R> it){
        return POrderedSetX.fromIterable(()->it);
    }
    
    
}
