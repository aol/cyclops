package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.util.stream.StreamUtils;

public interface SetX<T> extends Set<T>, MutableCollectionX<T> {
	static <T> Collector<T,?,Set<T>> defaultCollector(){
		return Collectors.toCollection(()-> new HashSet<>());
	}
	static <T> Collector<T,?,Set<T>> immutableCollector(){
		return Collectors.collectingAndThen(defaultCollector(), (Set<T> d)->Collections.unmodifiableSet(d));

	}
	
	public static <T> SetX<T> empty(){
		return fromIterable((Set<T>) defaultCollector().supplier().get());
	}
	@SafeVarargs
	public static <T> SetX<T> of(T...values){
		Set<T> res = (Set<T>) defaultCollector().supplier().get();
		for(T v: values)
			res.add(v);
		return  fromIterable(res);
	}
	public static <T> SetX<T> singleton(T value){
		return SetX.<T>of(value);
	}
    /**
     * Construct a SetX from an Publisher
     * 
     * @param iterable
     *            to construct SetX from
     * @return SetX
     */
    public static <T> SetX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toSetX();
    }
	public static <T> SetX<T> fromIterable(Iterable<T> it){
		return fromIterable(defaultCollector(),it);
	}
	public static <T> SetX<T> fromIterable(Collector<T,?,Set<T>>  collector,Iterable<T> it){
		if(it instanceof SetX)
			return (SetX)it;
		if(it instanceof Set)
			return new SetXImpl<T>( (Set)it, collector);
		return new SetXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
    /**
     * Efficiently make this List immutable
     * 
     * <pre>
     * {@code 
     * 
     *    ListX.of(1,2,3,4)
     *         .immutable();
     *         .plus(10); //Exception!
     * }
     * </pre>
     * 
     * @return
     */
    public FluentCollectionX<T> immutable();
	  /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
     */
    @Override
    <R> SetX<R> unit(Collection<R> col);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    <R> SetX<R> unit(R value);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    <R> SetX<R> unitIterator(Iterator<R> it);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    <R> SetX<R> patternMatch(
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
    SetX<T> reverse();
    /**
     * Combine two adjacent elements in a SetX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  SetX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toSetX()
                   
     *  //SetX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced SetX
     */
    @Override
    SetX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    SetX<T> filter(Predicate<? super T> pred);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
     */
    @Override
    <R> SetX<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    <R> SetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
     */
    @Override
    SetX<T> limit(long num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
     */
    @Override
    SetX<T> skip(long num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeRight(int)
     */
    @Override
    SetX<T> takeRight(int num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#dropRight(int)
     */
    @Override
    SetX<T> dropRight(int num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    SetX<T> takeWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    SetX<T> dropWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    SetX<T> takeUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    SetX<T> dropUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    <R> SetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
     */
    @Override
    SetX<T> slice(long from, long to);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
     */
    @Override
    <U extends Comparable<? super U>> SetX<T> sorted(Function<? super T, ? extends U> function);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int)
     */
    @Override
    SetX<ListX<T>> grouped(int groupSize);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override 
    <K, A, D> SetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function)
     */
    @Override
    <K> SetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable)
     */
    @Override
    <U> SetX<Tuple2<T, U>> zip(Iterable<U> other);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    <U, R> SetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int)
     */
    @Override
    SetX<ListX<T>> sliding(int windowSize);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int, int)
     */
    @Override
    SetX<ListX<T>> sliding(int windowSize, int increment);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    SetX<T> scanLeft(Monoid<T> monoid);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> SetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    SetX<T> scanRight(Monoid<T> monoid);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> SetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);
    

    /* Lazy operation
     * 
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plus(java.lang.Object)
     */
    @Override
    SetX<T> plus(T e);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plusAll(java.util.Collection)
     */
    @Override
    SetX<T> plusAll(Collection<? extends T> list);
    
 
    
    /*
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minus(java.lang.Object)
     */
    SetX<T> minus(Object e);
    
    /* 
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minusAll(java.util.Collection)
     */
    SetX<T> minusAll(Collection<?> list);
    


    @Override
    int size();

   

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default SetX<T> peek(Consumer<? super T> c) {
        return (SetX<T>)MutableCollectionX.super.peek(c);
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
    <U> SetX<Tuple2<T, U>> zipStream(Stream<U> other);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq)
     */
    @Override
    <U> SetX<Tuple2<T, U>> zip(Seq<U> other) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <S, U> SetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <T2, T3, T4> SetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zipWithIndex()
     */
    @Override
    SetX<Tuple2<T, Long>> zipWithIndex();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted()
     */
    @Override
    SetX<T> sorted();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
     */
    @Override
    SetX<T> sorted(Comparator<? super T> c);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    SetX<T> skipWhile(Predicate<? super T> p);
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    SetX<T> skipUntil(Predicate<? super T> p);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle()
     */
    @Override
    SetX<T> shuffle();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipLast(int)
     */
    @Override
    SetX<T> skipLast(int num) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
     */
    @Override
    SetX<T> shuffle(Random random);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#permutations()
     */
    @Override
    SetX<ReactiveSeq<T>> permutations();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations(int)
     */
    @Override
    SetX<ReactiveSeq<T>> combinations(int size);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations()
     */
    @Override
    SetX<ReactiveSeq<T>> combinations();
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cast(java.lang.Class)
     */
    @Override
    public <U> SetX<U> cast(Class<U> type);

    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#distinct()
     */
    @Override
    SetX<T> distinct();

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    SetX<T> limitWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    SetX<T> limitUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
     */
    @Override
    SetX<T> intersperse(T value);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
     */
    @Override
    SetX<T> limitLast(int num);
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    SetX<T> onEmpty(T value);
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    SetX<T> onEmptyGet(Supplier<T> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    <X extends Throwable> SetX<T> onEmptyThrow(Supplier<X> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
     */
    @Override
    <U> SetX<U> ofType(Class<U> type);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    SetX<T> filterNot(Predicate<? super T> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
     */
    @Override
    SetX<T> notNull();

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    SetX<T> removeAll(Stream<T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    SetX<T> removeAll(Iterable<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    SetX<T> removeAll(T... values);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
     */
    @Override
    SetX<T> retainAll(Iterable<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
     */
    @Override
    SetX<T> retainAll(Stream<T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
     */
    @Override
    SetX<T> retainAll(T... values);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> SetX<C> grouped(int size, Supplier<C> supplier);

     /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
     SetX<ListX<T>> groupedUntil(Predicate<? super T> predicate);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
     SetX<ListX<T>> groupedWhile(Predicate<? super T> predicate);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> SetX<C> groupedWhile(Predicate<? super T> predicate,
                Supplier<C> factory);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> SetX<C> groupedUntil(Predicate<? super T> predicate,
                Supplier<C> factory);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    SetX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate);
        
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    SetX<T> removeAll(Seq<T> stream);


     /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
     SetX<T> retainAll(Seq<T> stream);
}
