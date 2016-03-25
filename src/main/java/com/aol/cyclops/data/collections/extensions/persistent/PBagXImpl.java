package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
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
import org.pcollections.PBag;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.AbstractFluentCollectionX;
import com.aol.cyclops.data.collections.extensions.LazyFluentCollection;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

import lombok.AllArgsConstructor;


@AllArgsConstructor
public class PBagXImpl<T> extends AbstractFluentCollectionX<T> implements PBagX<T> {
	
	
	private final  LazyFluentCollection<T,PBag<T>> lazy;
	public PBagXImpl(PBag<T> bag){
	    this.lazy = new PersistentLazyCollection<>(bag,null,Reducers.toPBag());
	}
	public PBagXImpl(Stream<T> stream){
        this.lazy = new PersistentLazyCollection<>(null,stream,Reducers.toPBag());
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
	public PBagX<T> plus(T e) {
		return new PBagXImpl<>(getSet().plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  PBagX<T> minus(Object e) {
		return new PBagXImpl<>(getSet().minus(e));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  PBagX<T> plusAll(Collection<? extends T> list) {
		return  new PBagXImpl<>(getSet().plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public PBagX<T> minusAll(Collection<?> list) {
		return  new PBagXImpl<>(getSet().minusAll(list));
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

    private PBag<T> getSet() {
        return lazy.get();
    }
    public <X> PBagX<X> stream(Stream<X> stream){
        return new PBagXImpl<X>(stream);
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
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public PBagX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (PBagX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#reverse()
     */
    @Override
    public PBagX<T> reverse() {
       
        return(PBagX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#filter(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> filter(Predicate<? super T> pred) {
       
        return (PBagX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#map(java.util.function.Function)
     */
    @Override
    public <R> PBagX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (PBagX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> PBagX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (PBagX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#limit(long)
     */
    @Override
    public PBagX<T> limit(long num) {
       return (PBagX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#skip(long)
     */
    @Override
    public PBagX<T> skip(long num) {
       return (PBagX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#takeRight(int)
     */
    @Override
    public PBagX<T> takeRight(int num) {
       return (PBagX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#dropRight(int)
     */
    @Override
    public PBagX<T> dropRight(int num) {
       return (PBagX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> takeWhile(Predicate<? super T> p) {
       return (PBagX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> dropWhile(Predicate<? super T> p) {
       return (PBagX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> takeUntil(Predicate<? super T> p) {
       return (PBagX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> dropUntil(Predicate<? super T> p) {
       return(PBagX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> PBagX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (PBagX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#slice(long, long)
     */
    @Override
    public PBagX<T> slice(long from, long to) {
       return (PBagX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#grouped(int)
     */
    @Override
    public PBagX<ListX<T>> grouped(int groupSize) {
       
        return (PBagX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> PBagX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (PBagX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#grouped(java.util.function.Function)
     */
    @Override
    public <K> PBagX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (PBagX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#zip(java.lang.Iterable)
     */
    @Override
    public <U> PBagX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (PBagX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> PBagX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (PBagX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#sliding(int)
     */
    @Override
    public PBagX<ListX<T>> sliding(int windowSize) {
       
        return (PBagX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#sliding(int, int)
     */
    @Override
    public PBagX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (PBagX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public PBagX<T> scanLeft(Monoid<T> monoid) {
       
        return (PBagX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PBagX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (PBagX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public PBagX<T> scanRight(Monoid<T> monoid) {
       
        return (PBagX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PBagX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (PBagX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> PBagX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (PBagX<T>)super.sorted(function);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#plusLazy(java.lang.Object)
     */
    @Override
    public PBagX<T> plusLazy(T e) {
       
        return (PBagX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#plusAllLazy(java.util.Collection)
     */
    @Override
    public PBagX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (PBagX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#minusLazy(java.lang.Object)
     */
    @Override
    public PBagX<T> minusLazy(Object e) {
       
        return (PBagX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#minusAllLazy(java.util.Collection)
     */
    @Override
    public PBagX<T> minusAllLazy(Collection<?> list) {
       
        return (PBagX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(int)
     */
    @Override
    public PBagX<T> cycle(int times) {
        
        return stream(this.stream().cycle(times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    public PBagX<T> cycle(Monoid<T> m, int times) {
        
        return stream(this.stream().cycle(m,times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> cycleWhile(Predicate<? super T> predicate) {
        
        return stream(this.stream().cycleWhile(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> cycleUntil(Predicate<? super T> predicate) {
        
        return stream(this.stream().cycleUntil(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> PBagX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (PBagX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> PBagX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (PBagX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> PBagX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (PBagX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> PBagX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (PBagX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#zipWithIndex()
     */
    @Override
    public PBagX<Tuple2<T, Long>> zipWithIndex() {
       
        return (PBagX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#distinct()
     */
    @Override
    public PBagX<T> distinct() {
       
        return (PBagX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#sorted()
     */
    @Override
    public PBagX<T> sorted() {
       
        return (PBagX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#sorted(java.util.Comparator)
     */
    @Override
    public PBagX<T> sorted(Comparator<? super T> c) {
       
        return (PBagX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> skipWhile(Predicate<? super T> p) {
       
        return (PBagX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> skipUntil(Predicate<? super T> p) {
       
        return (PBagX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> limitWhile(Predicate<? super T> p) {
       
        return (PBagX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> limitUntil(Predicate<? super T> p) {
       
        return (PBagX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#intersperse(java.lang.Object)
     */
    @Override
    public PBagX<T> intersperse(T value) {
       
        return (PBagX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#shuffle()
     */
    @Override
    public PBagX<T> shuffle() {
       
        return (PBagX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#skipLast(int)
     */
    @Override
    public PBagX<T> skipLast(int num) {
       
        return (PBagX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#limitLast(int)
     */
    @Override
    public PBagX<T> limitLast(int num) {
       
        return (PBagX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#onEmpty(java.lang.Object)
     */
    @Override
    public PBagX<T> onEmpty(T value) {
       
        return (PBagX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public PBagX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (PBagX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> PBagX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (PBagX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#shuffle(java.util.Random)
     */
    @Override
    public PBagX<T> shuffle(Random random) {
       
        return (PBagX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#ofType(java.lang.Class)
     */
    @Override
    public <U> PBagX<U> ofType(Class<U> type) {
       
        return (PBagX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#filterNot(java.util.function.Predicate)
     */
    @Override
    public PBagX<T> filterNot(Predicate<? super T> fn) {
       
        return (PBagX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#notNull()
     */
    @Override
    public PBagX<T> notNull() {
       
        return (PBagX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#removeAll(java.util.stream.Stream)
     */
    @Override
    public PBagX<T> removeAll(Stream<T> stream) {
       
        return (PBagX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public PBagX<T> removeAll(Seq<T> stream) {
       
        return (PBagX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#removeAll(java.lang.Iterable)
     */
    @Override
    public PBagX<T> removeAll(Iterable<T> it) {
       
        return (PBagX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#removeAll(java.lang.Object[])
     */
    @Override
    public PBagX<T> removeAll(T... values) {
       
        return (PBagX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#retainAll(java.lang.Iterable)
     */
    @Override
    public PBagX<T> retainAll(Iterable<T> it) {
       
        return (PBagX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#retainAll(java.util.stream.Stream)
     */
    @Override
    public PBagX<T> retainAll(Stream<T> stream) {
       
        return (PBagX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public PBagX<T> retainAll(Seq<T> stream) {
       
        return (PBagX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#retainAll(java.lang.Object[])
     */
    @Override
    public PBagX<T> retainAll(T... values) {
       
        return (PBagX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#cast(java.lang.Class)
     */
    @Override
    public <U> PBagX<U> cast(Class<U> type) {
       
        return (PBagX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> PBagX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (PBagX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#permutations()
     */
    @Override
    public PBagX<ReactiveSeq<T>> permutations() {
       
        return (PBagX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#combinations(int)
     */
    @Override
    public PBagX<ReactiveSeq<T>> combinations(int size) {
       
        return (PBagX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#combinations()
     */
    @Override
    public PBagX<ReactiveSeq<T>> combinations() {
       
        return (PBagX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PBagX<C> grouped(int size, Supplier<C> supplier) {
       
        return (PBagX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public PBagX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (PBagX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public PBagX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (PBagX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PBagX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PBagX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PBagX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PBagX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PBagX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public PBagX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (PBagX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** PBagX methods **/

    /* Makes a defensive copy of this PBagX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableSequenceX#with(int, java.lang.Object)
     */
    public PBagX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> PBagX<R> unit(Collection<R> col){
        return PBagX.fromIterable(col);
    }
    @Override
    public  <R> PBagX<R> unit(R value){
        return PBagX.singleton(value);
    }
    @Override
    public <R> PBagX<R> unitIterator(Iterator<R> it){
        return PBagX.fromIterable(()->it);
    }
    
	

}
