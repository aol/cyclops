package com.aol.cyclops.data.collections.extensions.standard;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.util.stream.StreamUtils;

public interface SortedSetX<T> extends SortedSet<T>,MutableCollectionX<T> {
	static <T> Collector<T,?,SortedSet<T>> defaultCollector(){
		return Collectors.toCollection(()-> new TreeSet<T>((Comparator)Comparator.<Comparable>naturalOrder()));
	}
	static <T> Collector<T,?,SortedSet<T>> immutableCollector(){
		return Collectors.collectingAndThen(defaultCollector(), (SortedSet<T> d)->Collections.unmodifiableSortedSet(d));

	}
	
	public static <T> SortedSetX<T> empty(){
		return fromIterable((SortedSet<T>) defaultCollector().supplier().get());
	}
	public static <T> SortedSetX<T> of(T...values){
		SortedSet<T> res = (SortedSet<T>) defaultCollector().supplier().get();
		for(T v: values)
			res.add(v);
		return  fromIterable(res);
	}
	public static <T> SortedSetX<T> singleton(T value){
		return of(value);
	}
    /**
     * Construct a SortedSetX from an Publisher
     * 
     * @param iterable
     *            to construct SortedSetX from
     * @return SortedSetX
     */
    public static <T> SortedSetX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toSortedSetX();
    }
	public static <T> SortedSetX<T> fromIterable(Iterable<T> it){
		return fromIterable(defaultCollector(),it);
	}
	public static <T> SortedSetX<T> fromIterable(Collector<T,?,SortedSet<T>>  collector,Iterable<T> it){
		if(it instanceof SortedSetX)
			return (SortedSetX<T>)it;
		if(it instanceof SortedSet)
			return new SortedSetXImpl<T>( (SortedSet)it, collector);
		return new SortedSetXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
	@Override
	public <X> SortedSetX<X> stream(Stream<X> stream);
	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
	 */
	@Override
	default SortedSetX<T> toSortedSetX() {
		return this;
	}
	  /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
     */
    @Override
    <R> SortedSetX<R> unit(Collection<R> col);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    <R> SortedSetX<R> unit(R value);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    <R> SortedSetX<R> unitIterator(Iterator<R> it);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    <R> SortedSetX<R> patternMatch(
            Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise);
    
    /* (non-Javadoc)
     * @see java.util.Collection#stream()
     */
    @Override
    ReactiveSeq<T> stream();
  

    
    
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
     */
    @Override
    SortedSetX<T> reverse();
    /**
     * Combine two adjacent elements in a SortedSetX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  SortedSetX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toSortedSetX()
                   
     *  //SortedSetX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced SortedSetX
     */
    @Override
    SortedSetX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> filter(Predicate<? super T> pred);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
     */
    @Override
    <R> SortedSetX<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    <R> SortedSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
     */
    @Override
    SortedSetX<T> limit(long num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
     */
    @Override
    SortedSetX<T> skip(long num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeRight(int)
     */
    @Override
    SortedSetX<T> takeRight(int num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#dropRight(int)
     */
    @Override
    SortedSetX<T> dropRight(int num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> takeWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> dropWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> takeUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> dropUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    <R> SortedSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
     */
    @Override
    SortedSetX<T> slice(long from, long to);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
     */
    @Override
    <U extends Comparable<? super U>> SortedSetX<T> sorted(Function<? super T, ? extends U> function);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int)
     */
    @Override
    SortedSetX<ListX<T>> grouped(int groupSize);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override 
    <K, A, D> SortedSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function)
     */
    @Override
    <K> SortedSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable)
     */
    @Override
    <U> SortedSetX<Tuple2<T, U>> zip(Iterable<U> other);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    <U, R> SortedSetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int)
     */
    @Override
    SortedSetX<ListX<T>> sliding(int windowSize);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int, int)
     */
    @Override
    SortedSetX<ListX<T>> sliding(int windowSize, int increment);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    SortedSetX<T> scanLeft(Monoid<T> monoid);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> SortedSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    SortedSetX<T> scanRight(Monoid<T> monoid);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> SortedSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);
    

    /* Lazy operation
     * 
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plus(java.lang.Object)
     */
    @Override
    SortedSetX<T> plus(T e);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plusAll(java.util.Collection)
     */
    @Override
    SortedSetX<T> plusAll(Collection<? extends T> list);
    
 
    
    /*
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minus(java.lang.Object)
     */
    SortedSetX<T> minus(Object e);
    
    /* 
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minusAll(java.util.Collection)
     */
    SortedSetX<T> minusAll(Collection<?> list);
    


    @Override
    int size();

   

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default SortedSetX<T> peek(Consumer<? super T> c) {
        return (SortedSetX<T>)MutableCollectionX.super.peek(c);
    }
    



    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cycle(int)
     */
    @Override
    ListX<T> cycle(int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    ListX<T> cycle(Monoid<T> m, int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    ListX<T> cycleWhile(Predicate<? super T> predicate);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    ListX<T> cycleUntil(Predicate<? super T> predicate) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zipStream(java.util.stream.Stream)
     */
    @Override
    <U> SortedSetX<Tuple2<T, U>> zipStream(Stream<U> other);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq)
     */
    @Override
    <U> SortedSetX<Tuple2<T, U>> zip(Seq<U> other) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <S, U> SortedSetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <T2, T3, T4> SortedSetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zipWithIndex()
     */
    @Override
    SortedSetX<Tuple2<T, Long>> zipWithIndex();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted()
     */
    @Override
    SortedSetX<T> sorted();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
     */
    @Override
    SortedSetX<T> sorted(Comparator<? super T> c);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> skipWhile(Predicate<? super T> p);
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> skipUntil(Predicate<? super T> p);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle()
     */
    @Override
    SortedSetX<T> shuffle();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipLast(int)
     */
    @Override
    SortedSetX<T> skipLast(int num) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
     */
    @Override
    SortedSetX<T> shuffle(Random random);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#permutations()
     */
    @Override
    SortedSetX<ReactiveSeq<T>> permutations();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations(int)
     */
    @Override
    SortedSetX<ReactiveSeq<T>> combinations(int size);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations()
     */
    @Override
    SortedSetX<ReactiveSeq<T>> combinations();
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cast(java.lang.Class)
     */
    @Override
    public <U> SortedSetX<U> cast(Class<U> type);

    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#distinct()
     */
    @Override
    SortedSetX<T> distinct();

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> limitWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> limitUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
     */
    @Override
    SortedSetX<T> intersperse(T value);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
     */
    @Override
    SortedSetX<T> limitLast(int num);
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    SortedSetX<T> onEmpty(T value);
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    SortedSetX<T> onEmptyGet(Supplier<T> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    <X extends Throwable> SortedSetX<T> onEmptyThrow(Supplier<X> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
     */
    @Override
    <U> SortedSetX<U> ofType(Class<U> type);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    SortedSetX<T> filterNot(Predicate<? super T> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
     */
    @Override
    SortedSetX<T> notNull();

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    SortedSetX<T> removeAll(Stream<T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    SortedSetX<T> removeAll(Iterable<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    SortedSetX<T> removeAll(T... values);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
     */
    @Override
    SortedSetX<T> retainAll(Iterable<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
     */
    @Override
    SortedSetX<T> retainAll(Stream<T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
     */
    @Override
    SortedSetX<T> retainAll(T... values);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> SortedSetX<C> grouped(int size, Supplier<C> supplier);

     /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
     SortedSetX<ListX<T>> groupedUntil(Predicate<? super T> predicate);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
     SortedSetX<ListX<T>> groupedWhile(Predicate<? super T> predicate);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> SortedSetX<C> groupedWhile(Predicate<? super T> predicate,
                Supplier<C> factory);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> SortedSetX<C> groupedUntil(Predicate<? super T> predicate,
                Supplier<C> factory);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    SortedSetX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate);
        
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    SortedSetX<T> removeAll(Seq<T> stream);


     /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
     SortedSetX<T> retainAll(Seq<T> stream);
}
