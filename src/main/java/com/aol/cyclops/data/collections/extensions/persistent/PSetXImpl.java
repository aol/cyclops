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
import org.pcollections.PSet;

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
public class PSetXImpl<T> extends AbstractFluentCollectionX<T> implements PSetX<T> {
	
	
	private final LazyFluentCollection<T,PSet<T>> lazy;
    public PSetXImpl(PSet<T> set){
        this.lazy = new PersistentLazyCollection<>(set,null,Reducers.toPSet());
    }
    private PSetXImpl(Stream<T> stream){
        this.lazy = new PersistentLazyCollection<>(null,stream,Reducers.toPSet());
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
	public PSetX<T> plus(T e) {
		return new PSetXImpl<>(getSet().plus(e));
	}

	/**
	 * @param e
	 * @return
	 * @see org.pcollections.MapPSet#minus(java.lang.Object)
	 */
	public  PSetX<T> minus(Object e) {
		return new PSetXImpl<>(getSet().minus(e));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#plusAll(java.util.Collection)
	 */
	public  PSetX<T> plusAll(Collection<? extends T> list) {
		return  new PSetXImpl<>(getSet().plusAll(list));
	}

	/**
	 * @param list
	 * @return
	 * @see org.pcollections.MapPSet#minusAll(java.util.Collection)
	 */
	public PSetX<T> minusAll(Collection<?> list) {
		return  new PSetXImpl<>(getSet().minusAll(list));
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

    private PSet<T> getSet() {
        return lazy.get();
    }
    public <X> PSetX<X> stream(Stream<X> stream){
        return new PSetXImpl<X>(stream);
    }
    
    
   

   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#stream()
     */
    @Override
    public ReactiveSeq<T> streamInternal() {
        return lazy.stream();
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#stream()
     */
    @Override
    public ReactiveSeq<T> stream() {
        return ReactiveSeq.fromStream(lazy.get().stream());
    }
   
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public PSetX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (PSetX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#reverse()
     */
    @Override
    public PSetX<T> reverse() {
       
        return(PSetX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#filter(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> filter(Predicate<? super T> pred) {
       
        return (PSetX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#map(java.util.function.Function)
     */
    @Override
    public <R> PSetX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (PSetX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> PSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (PSetX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#limit(long)
     */
    @Override
    public PSetX<T> limit(long num) {
       return (PSetX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#skip(long)
     */
    @Override
    public PSetX<T> skip(long num) {
       return (PSetX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#takeRight(int)
     */
    @Override
    public PSetX<T> takeRight(int num) {
       return (PSetX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#dropRight(int)
     */
    @Override
    public PSetX<T> dropRight(int num) {
       return (PSetX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> takeWhile(Predicate<? super T> p) {
       return (PSetX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> dropWhile(Predicate<? super T> p) {
       return (PSetX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> takeUntil(Predicate<? super T> p) {
       return (PSetX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> dropUntil(Predicate<? super T> p) {
       return(PSetX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> PSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (PSetX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#slice(long, long)
     */
    @Override
    public PSetX<T> slice(long from, long to) {
       return (PSetX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#grouped(int)
     */
    @Override
    public PSetX<ListX<T>> grouped(int groupSize) {
       
        return (PSetX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> PSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (PSetX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#grouped(java.util.function.Function)
     */
    @Override
    public <K> PSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (PSetX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#zip(java.lang.Iterable)
     */
    @Override
    public <U> PSetX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (PSetX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> PSetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (PSetX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#sliding(int)
     */
    @Override
    public PSetX<ListX<T>> sliding(int windowSize) {
       
        return (PSetX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#sliding(int, int)
     */
    @Override
    public PSetX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (PSetX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public PSetX<T> scanLeft(Monoid<T> monoid) {
       
        return (PSetX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (PSetX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public PSetX<T> scanRight(Monoid<T> monoid) {
       
        return (PSetX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> PSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (PSetX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> PSetX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (PSetX<T>)super.sorted(function);
    }
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#plusLazy(java.lang.Object)
     */
    @Override
    public PSetX<T> plusLazy(T e) {
       
        return (PSetX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#plusAllLazy(java.util.Collection)
     */
    @Override
    public PSetX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (PSetX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#minusLazy(java.lang.Object)
     */
    @Override
    public PSetX<T> minusLazy(Object e) {
       
        return (PSetX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#minusAllLazy(java.util.Collection)
     */
    @Override
    public PSetX<T> minusAllLazy(Collection<?> list) {
       
        return (PSetX<T>)super.minusAll(list);
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
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> PSetX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (PSetX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> PSetX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (PSetX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> PSetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (PSetX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> PSetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (PSetX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#zipWithIndex()
     */
    @Override
    public PSetX<Tuple2<T, Long>> zipWithIndex() {
       
        return (PSetX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#distinct()
     */
    @Override
    public PSetX<T> distinct() {
       
        return (PSetX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#sorted()
     */
    @Override
    public PSetX<T> sorted() {
       
        return (PSetX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#sorted(java.util.Comparator)
     */
    @Override
    public PSetX<T> sorted(Comparator<? super T> c) {
       
        return (PSetX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> skipWhile(Predicate<? super T> p) {
       
        return (PSetX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> skipUntil(Predicate<? super T> p) {
       
        return (PSetX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> limitWhile(Predicate<? super T> p) {
       
        return (PSetX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> limitUntil(Predicate<? super T> p) {
       
        return (PSetX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#intersperse(java.lang.Object)
     */
    @Override
    public PSetX<T> intersperse(T value) {
       
        return (PSetX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#shuffle()
     */
    @Override
    public PSetX<T> shuffle() {
       
        return (PSetX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#skipLast(int)
     */
    @Override
    public PSetX<T> skipLast(int num) {
       
        return (PSetX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#limitLast(int)
     */
    @Override
    public PSetX<T> limitLast(int num) {
       
        return (PSetX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#onEmpty(java.lang.Object)
     */
    @Override
    public PSetX<T> onEmpty(T value) {
       
        return (PSetX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public PSetX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (PSetX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> PSetX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (PSetX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#shuffle(java.util.Random)
     */
    @Override
    public PSetX<T> shuffle(Random random) {
       
        return (PSetX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#ofType(java.lang.Class)
     */
    @Override
    public <U> PSetX<U> ofType(Class<U> type) {
       
        return (PSetX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#filterNot(java.util.function.Predicate)
     */
    @Override
    public PSetX<T> filterNot(Predicate<? super T> fn) {
       
        return (PSetX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#notNull()
     */
    @Override
    public PSetX<T> notNull() {
       
        return (PSetX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#removeAll(java.util.stream.Stream)
     */
    @Override
    public PSetX<T> removeAll(Stream<T> stream) {
       
        return (PSetX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public PSetX<T> removeAll(Seq<T> stream) {
       
        return (PSetX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#removeAll(java.lang.Iterable)
     */
    @Override
    public PSetX<T> removeAll(Iterable<T> it) {
       
        return (PSetX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#removeAll(java.lang.Object[])
     */
    @Override
    public PSetX<T> removeAll(T... values) {
       
        return (PSetX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#retainAll(java.lang.Iterable)
     */
    @Override
    public PSetX<T> retainAll(Iterable<T> it) {
       
        return (PSetX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#retainAll(java.util.stream.Stream)
     */
    @Override
    public PSetX<T> retainAll(Stream<T> stream) {
       
        return (PSetX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public PSetX<T> retainAll(Seq<T> stream) {
       
        return (PSetX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#retainAll(java.lang.Object[])
     */
    @Override
    public PSetX<T> retainAll(T... values) {
       
        return (PSetX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#cast(java.lang.Class)
     */
    @Override
    public <U> PSetX<U> cast(Class<U> type) {
       
        return (PSetX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> PSetX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (PSetX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#permutations()
     */
    @Override
    public PSetX<ReactiveSeq<T>> permutations() {
       
        return (PSetX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#combinations(int)
     */
    @Override
    public PSetX<ReactiveSeq<T>> combinations(int size) {
       
        return (PSetX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#combinations()
     */
    @Override
    public PSetX<ReactiveSeq<T>> combinations() {
       
        return (PSetX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PSetX<C> grouped(int size, Supplier<C> supplier) {
       
        return (PSetX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public PSetX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (PSetX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public PSetX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (PSetX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PSetX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PSetX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> PSetX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (PSetX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.persistent.PSetX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public PSetX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (PSetX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** PSetX methods **/

    /* Makes a defensive copy of this PSetX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.persistent.MutableSequenceX#with(int, java.lang.Object)
     */
    public PSetX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> PSetX<R> unit(Collection<R> col){
        return PSetX.fromIterable(col);
    }
    @Override
    public  <R> PSetX<R> unit(R value){
        return PSetX.singleton(value);
    }
    @Override
    public <R> PSetX<R> unitIterator(Iterator<R> it){
        return PSetX.fromIterable(()->it);
    }
    
    
    
}
