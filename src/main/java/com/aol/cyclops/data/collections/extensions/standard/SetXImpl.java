package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;
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
import com.aol.cyclops.data.collections.extensions.AbstractFluentCollectionX;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
public class SetXImpl<T> extends AbstractFluentCollectionX<T> implements SetX<T> {
	
    private final LazyCollection<T,Set<T>> lazy;
	@Getter
	private final Collector<T,?,Set<T>> collector;
	
	   public SetXImpl(Set<T> Set,Collector<T,?,Set<T>> collector){
	        this.lazy = new LazyCollection<>(Set,null,collector);
	        this.collector=  collector;
	    }
	    public SetXImpl(Set<T> Set){
	        
	        this.collector = SetX.defaultCollector();
	        this.lazy = new LazyCollection<T,Set<T>>(Set,null,collector);
	    }
	    private SetXImpl(Stream<T> stream){
	        
	        this.collector = SetX.defaultCollector();
	        this.lazy = new LazyCollection<>(null,stream,collector);
	    }
	    public SetXImpl(){
	        this.collector = SetX.defaultCollector();
	        this.lazy = new LazyCollection<>((Set)this.collector.supplier().get(),null,collector);
	    }
	    
	@Override
	public FluentCollectionX<T> immutable(){
	        return new SetXImpl<>(Collections.unmodifiableSet(getSet()),SetX.immutableCollector());
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
    private Set<T> getSet() {
        return lazy.get();
    }
   
    
   
    @Override
    public <X> SetX<X> stream(Stream<X> stream){
        return new SetXImpl<X>(stream);
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
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    public SetX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
       
        return (SetX<T>)super.combine(predicate, op);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#reverse()
     */
    @Override
    public SetX<T> reverse() {
       
        return(SetX<T>)super.reverse();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#filter(java.util.function.Predicate)
     */
    @Override
    public SetX<T> filter(Predicate<? super T> pred) {
       
        return (SetX<T>)super.filter(pred);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#map(java.util.function.Function)
     */
    @Override
    public <R> SetX<R> map(Function<? super T, ? extends R> mapper) {
       
        return (SetX<R>)super.map(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#flatMap(java.util.function.Function)
     */
    @Override
    public <R> SetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
       return (SetX<R>)super.flatMap(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#limit(long)
     */
    @Override
    public SetX<T> limit(long num) {
       return (SetX<T>)super.limit(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#skip(long)
     */
    @Override
    public SetX<T> skip(long num) {
       return (SetX<T>)super.skip(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#takeRight(int)
     */
    @Override
    public SetX<T> takeRight(int num) {
       return (SetX<T>)super.takeRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#dropRight(int)
     */
    @Override
    public SetX<T> dropRight(int num) {
       return (SetX<T>)super.dropRight(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#takeWhile(java.util.function.Predicate)
     */
    @Override
    public SetX<T> takeWhile(Predicate<? super T> p) {
       return (SetX<T>)super.takeWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#dropWhile(java.util.function.Predicate)
     */
    @Override
    public SetX<T> dropWhile(Predicate<? super T> p) {
       return (SetX<T>)super.dropWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#takeUntil(java.util.function.Predicate)
     */
    @Override
    public SetX<T> takeUntil(Predicate<? super T> p) {
       return (SetX<T>)super.takeUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#dropUntil(java.util.function.Predicate)
     */
    @Override
    public SetX<T> dropUntil(Predicate<? super T> p) {
       return(SetX<T>)super.dropUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#trampoline(java.util.function.Function)
     */
    @Override
    public <R> SetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
       return (SetX<R>)super.trampoline(mapper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#slice(long, long)
     */
    @Override
    public SetX<T> slice(long from, long to) {
       return (SetX<T>)super.slice(from, to);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#grouped(int)
     */
    @Override
    public SetX<ListX<T>> grouped(int groupSize) {
       
        return (SetX<ListX<T>>)super.grouped(groupSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override
    public <K, A, D> SetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier,
            Collector<? super T, A, D> downstream) {
       
        return (SetX)super.grouped(classifier, downstream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#grouped(java.util.function.Function)
     */
    @Override
    public <K> SetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier) {
       
        return (SetX)super.grouped(classifier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#zip(java.lang.Iterable)
     */
    @Override
    public <U> SetX<Tuple2<T, U>> zip(Iterable<U> other) {
       
        return (SetX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    public <U, R> SetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper) {
       
        return (SetX<R>)super.zip(other, zipper);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#sliding(int)
     */
    @Override
    public SetX<ListX<T>> sliding(int windowSize) {
       
        return (SetX<ListX<T>>)super.sliding(windowSize);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#sliding(int, int)
     */
    @Override
    public SetX<ListX<T>> sliding(int windowSize, int increment) {
       
        return (SetX<ListX<T>>)super.sliding(windowSize, increment);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    public SetX<T> scanLeft(Monoid<T> monoid) {
       
        return (SetX<T>)super.scanLeft(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> SetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function) {
       
        return (SetX<U>) super.scanLeft(seed, function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    public SetX<T> scanRight(Monoid<T> monoid) {
       
        return (SetX<T>)super.scanRight(monoid);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    public <U> SetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner) {
       
        return (SetX<U>)super.scanRight(identity, combiner);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#sorted(java.util.function.Function)
     */
    @Override
    public <U extends Comparable<? super U>> SetX<T> sorted(Function<? super T, ? extends U> function) {
       
        return (SetX<T>)super.sorted(function);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#plus(java.lang.Object)
     */
    @Override
    public SetX<T> plus(T e) {
       
        return (SetX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#plusAll(java.util.Collection)
     */
    @Override
    public SetX<T> plusAll(Collection<? extends T> list) {
       
        return (SetX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#minus(java.lang.Object)
     */
    @Override
    public SetX<T> minus(Object e) {
       
        return (SetX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#minusAll(java.util.Collection)
     */
    @Override
    public SetX<T> minusAll(Collection<?> list) {
       
        return (SetX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#plusLazy(java.lang.Object)
     */
    @Override
    public SetX<T> plusLazy(T e) {
       
        return (SetX<T>)super.plus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#plusAllLazy(java.util.Collection)
     */
    @Override
    public SetX<T> plusAllLazy(Collection<? extends T> list) {
       
        return (SetX<T>)super.plusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#minusLazy(java.lang.Object)
     */
    @Override
    public SetX<T> minusLazy(Object e) {
       
        return (SetX<T>)super.minus(e);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#minusAllLazy(java.util.Collection)
     */
    @Override
    public SetX<T> minusAllLazy(Collection<?> list) {
       
        return (SetX<T>)super.minusAll(list);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(int)
     */
    @Override
    public ListX<T> cycle(int times) {
        
        return new ListXImpl<T>(this.stream().cycle(times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
     */
    @Override
    public ListX<T> cycle(Monoid<T> m, int times) {
        
        return new ListXImpl<T>(this.stream().cycle(m,times));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleWhile(java.util.function.Predicate)
     */
    @Override
    public ListX<T> cycleWhile(Predicate<? super T> predicate) {
        
        return new ListXImpl<T>(this.stream().cycleWhile(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    public ListX<T> cycleUntil(Predicate<? super T> predicate) {
        
        return new ListXImpl<T>(this.stream().cycleUntil(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#zipStream(java.util.stream.Stream)
     */
    @Override
    public <U> SetX<Tuple2<T, U>> zipStream(Stream<U> other) {
       
        return (SetX<Tuple2<T, U>>)super.zipStream(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#zip(org.jooq.lambda.Seq)
     */
    @Override
    public <U> SetX<Tuple2<T, U>> zip(Seq<U> other) {
       
        return (SetX<Tuple2<T, U>>)super.zip(other);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <S, U> SetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
       
        return (SetX)super.zip3(second, third);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    public <T2, T3, T4> SetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) {
       
        return (SetX<Tuple4<T, T2, T3, T4>>)super.zip4(second, third, fourth);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#zipWithIndex()
     */
    @Override
    public SetX<Tuple2<T, Long>> zipWithIndex() {
       
        return (SetX<Tuple2<T, Long>>)super.zipWithIndex();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#distinct()
     */
    @Override
    public SetX<T> distinct() {
       
        return (SetX<T>)super.distinct();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#sorted()
     */
    @Override
    public SetX<T> sorted() {
       
        return (SetX<T>)super.sorted();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#sorted(java.util.Comparator)
     */
    @Override
    public SetX<T> sorted(Comparator<? super T> c) {
       
        return (SetX<T>)super.sorted(c);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#skipWhile(java.util.function.Predicate)
     */
    @Override
    public SetX<T> skipWhile(Predicate<? super T> p) {
       
        return (SetX<T>)super.skipWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#skipUntil(java.util.function.Predicate)
     */
    @Override
    public SetX<T> skipUntil(Predicate<? super T> p) {
       
        return (SetX<T>)super.skipUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#limitWhile(java.util.function.Predicate)
     */
    @Override
    public SetX<T> limitWhile(Predicate<? super T> p) {
       
        return (SetX<T>)super.limitWhile(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#limitUntil(java.util.function.Predicate)
     */
    @Override
    public SetX<T> limitUntil(Predicate<? super T> p) {
       
        return (SetX<T>)super.limitUntil(p);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#intersperse(java.lang.Object)
     */
    @Override
    public SetX<T> intersperse(T value) {
       
        return (SetX<T>)super.intersperse(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#shuffle()
     */
    @Override
    public SetX<T> shuffle() {
       
        return (SetX<T>)super.shuffle();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#skipLast(int)
     */
    @Override
    public SetX<T> skipLast(int num) {
       
        return (SetX<T>)super.skipLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#limitLast(int)
     */
    @Override
    public SetX<T> limitLast(int num) {
       
        return (SetX<T>)super.limitLast(num);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#onEmpty(java.lang.Object)
     */
    @Override
    public SetX<T> onEmpty(T value) {
       
        return (SetX<T>)super.onEmpty(value);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    public SetX<T> onEmptyGet(Supplier<T> supplier) {
       
        return (SetX<T>)super.onEmptyGet(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    public <X extends Throwable> SetX<T> onEmptyThrow(Supplier<X> supplier) {
       
        return (SetX<T>)super.onEmptyThrow(supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#shuffle(java.util.Random)
     */
    @Override
    public SetX<T> shuffle(Random random) {
       
        return (SetX<T>)super.shuffle(random);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#ofType(java.lang.Class)
     */
    @Override
    public <U> SetX<U> ofType(Class<U> type) {
       
        return (SetX<U>)super.ofType(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#filterNot(java.util.function.Predicate)
     */
    @Override
    public SetX<T> filterNot(Predicate<? super T> fn) {
       
        return (SetX<T>)super.filterNot(fn);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#notNull()
     */
    @Override
    public SetX<T> notNull() {
       
        return (SetX<T>)super.notNull();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#removeAll(java.util.stream.Stream)
     */
    @Override
    public SetX<T> removeAll(Stream<T> stream) {
       
        return (SetX<T>)(super.removeAll(stream));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    public SetX<T> removeAll(Seq<T> stream) {
       
        return (SetX<T>)super.removeAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#removeAll(java.lang.Iterable)
     */
    @Override
    public SetX<T> removeAll(Iterable<T> it) {
       
        return (SetX<T>)super.removeAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#removeAll(java.lang.Object[])
     */
    @Override
    public SetX<T> removeAll(T... values) {
       
        return (SetX<T>)super.removeAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#retainAll(java.lang.Iterable)
     */
    @Override
    public SetX<T> retainAll(Iterable<T> it) {
       
        return (SetX<T>)super.retainAll(it);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#retainAll(java.util.stream.Stream)
     */
    @Override
    public SetX<T> retainAll(Stream<T> stream) {
       
        return (SetX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
    public SetX<T> retainAll(Seq<T> stream) {
       
        return (SetX<T>)super.retainAll(stream);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#retainAll(java.lang.Object[])
     */
    @Override
    public SetX<T> retainAll(T... values) {
       
        return (SetX<T>)super.retainAll(values);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#cast(java.lang.Class)
     */
    @Override
    public <U> SetX<U> cast(Class<U> type) {
       
        return (SetX<U>)super.cast(type);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    public <R> SetX<R> patternMatch(Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,
            Supplier<? extends R> otherwise) {
       
        return (SetX<R>)super.patternMatch(case1, otherwise);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#permutations()
     */
    @Override
    public SetX<ReactiveSeq<T>> permutations() {
       
        return (SetX<ReactiveSeq<T>>)super.permutations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#combinations(int)
     */
    @Override
    public SetX<ReactiveSeq<T>> combinations(int size) {
       
        return (SetX<ReactiveSeq<T>>)super.combinations(size);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#combinations()
     */
    @Override
    public SetX<ReactiveSeq<T>> combinations() {
       
        return (SetX<ReactiveSeq<T>>)super.combinations();
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#grouped(int, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SetX<C> grouped(int size, Supplier<C> supplier) {
       
        return (SetX<C>)super.grouped(size, supplier);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    public SetX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
       
        return (SetX<ListX<T>>)super.groupedUntil(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    public SetX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
       
        return (SetX<ListX<T>>)super.groupedWhile(predicate);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SetX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (SetX<C>)super.groupedWhile(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    public <C extends Collection<? super T>> SetX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return (SetX<C>)super.groupedUntil(predicate, factory);
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.SetX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    public SetX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        
        return (SetX<ListX<T>>)super.groupedStatefullyWhile(predicate);
    }
    /** SetX methods **/

    /* Makes a defensive copy of this SetX replacing the value at i with the specified element
     *  (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
     */
    public SetX<T> with(int i,T element){
        return stream( streamInternal().deleteBetween(i, i+1).insertAt(i,element) ) ;
    }
   
    @Override
    public <R> SetX<R> unit(Collection<R> col){
        return SetX.fromIterable(col);
    }
    @Override
    public  <R> SetX<R> unit(R value){
        return SetX.singleton(value);
    }
    @Override
    public <R> SetX<R> unitIterator(Iterator<R> it){
        return SetX.fromIterable(()->it);
    }

}
