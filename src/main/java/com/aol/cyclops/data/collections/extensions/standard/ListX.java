package com.aol.cyclops.data.collections.extensions.standard;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
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
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.types.IterableFunctor;
import com.aol.cyclops.types.applicative.zipping.ZippingApplicativable;
import com.aol.cyclops.util.stream.StreamUtils;

/**
 * An extended JDK List. Extended operations (such as map, flatMap, filter etc) are lazy and efficient.
 * 
 * E.g. The code below will traverse the list only once, the traversal is triggered by the call to get(10)
 * 
 * <pre>
 * {@code 
 * 
 *  ListX.of(1,2,3,4,5)
 *       .map(i->i+2)
 *       .filter(i->i<5)
 *       .map(this::load)
 *       .get(10);
 *       
 *  
 * 
 * }
 * </pre>
 * 
 * ListX can extend any {@link java.util.List} implementation, by detault {@link java.util.ArrayList} is used. 
 * DequeX extends {@link java.util.LinkedList} by default. To extend other List types see @link {@link ListX#fromIterable(Collector, Iterable)}
 * passing an appropriate collector for your type ensures it is not converted to the default type after any extended operations.
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
public interface ListX<T> extends List<T>, 
                                  MutableCollectionX<T>, 
                                  MutableSequenceX<T>, 
                                  Comparable<T>,
                                  IterableFunctor<T>,
                                  ZippingApplicativable<T> {
	
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
    public CollectionX<T> immutable();
	/* Returns this
	 * 
	 * (non-Javadoc)
	 * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
	 */
	@Override
	default ListX<T> toListX() {
		return this;
	}

	
	
	/**
	 * @return The default collector - (which generates a mutable ArrayList)
	 */
	static <T> Collector<T,?,List<T>> defaultCollector(){
		return Collectors.toCollection(()-> new ArrayList<>());
	}
	/**
	 * @return An immutable version of the default collector
	 */
	static <T> Collector<T,?,List<T>> immutableCollector(){
		return Collectors.collectingAndThen(defaultCollector(), (List<T> d)->Collections.unmodifiableList(d));

	}
	
	/**
	 * @return An empty mutable extended List
	 */
	public static <T> ListX<T> empty(){
		return fromIterable((List<T>) defaultCollector().supplier().get());
	}
	/**
	 * Construct a new mutable extended List (in this case an ArrayList)
	 * <pre>
	 * {@code 
	 *    
	 *    ListX<Integer> list = ListX.of("hello","world")
	 *                               .map(str->str.length());
	 * }
	 * </code>
	 * 
	 * @param values to populate your List with
	 * @return
	 */
	@SafeVarargs
	public static <T> ListX<T> of(T...values){
		List<T> res = (List<T>) defaultCollector().supplier().get();
		for(T v: values)
			res.add(v);
		return  fromIterable(res);
	}
	public static <T> ListX<T> singleton(T value){
		return ListX.<T>of(value);
	}
	/**
     * Construct a ListX from an Publisher
     * 
     * @param iterable
     *            to construct ListX from
     * @return ListX
     */
    public static <T> ListX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toListX();
    }
	public static <T> ListX<T> fromIterable(Iterable<T> it){
		return fromIterable(defaultCollector(),it);
	}
	public static <T> ListX<T> fromIterable(Collector<T,?,List<T>>  collector,Iterable<T> it){
		
		if(it instanceof List)
			return new ListXImpl<T>( (List<T>)it, collector);
		return new ListXImpl<T>(StreamUtils.stream(it).collect(collector),collector);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
	 */
	@Override
	<R> ListX<R> unit(Collection<R> col);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
	 */
	@Override
	<R> ListX<R> unit(R value);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
	 */
	@Override
	<R> ListX<R> unitIterator(Iterator<R> it);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#patternMatch(java.util.function.Function, java.util.function.Supplier)
	 */
	@Override
    <R> ListX<R> patternMatch(
            Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise);
	
	/* (non-Javadoc)
	 * @see java.util.Collection#stream()
	 */
	@Override
	ReactiveSeq<T> stream();
	public <T> Collector<T,?,List<T>> getCollector();
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#from(java.util.Collection)
	 */
	//@Override
	//<T1> ListX<T1> from(Collection<T1> c);
	
	
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#reverse()
	 */
	@Override
	ListX<T> reverse();
	/**
     * Combine two adjacent elements in a ListX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  ListX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced ListX
     */
	@Override
    ListX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	ListX<T> filter(Predicate<? super T> pred);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	 */
	@Override
	<R> ListX<R> map(Function<? super T, ? extends R> mapper);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	<R> ListX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	 */
	@Override
	ListX<T> limit(long num);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	 */
	@Override
	ListX<T> skip(long num);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeRight(int)
	 */
	@Override
	ListX<T> takeRight(int num);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#dropRight(int)
	 */
	@Override
	ListX<T> dropRight(int num);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	ListX<T> takeWhile(Predicate<? super T> p);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	ListX<T> dropWhile(Predicate<? super T> p);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	ListX<T> takeUntil(Predicate<? super T> p);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	ListX<T> dropUntil(Predicate<? super T> p);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	<R> ListX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	 */
	@Override
	ListX<T> slice(long from, long to);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	<U extends Comparable<? super U>> ListX<T> sorted(Function<? super T, ? extends U> function);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int)
	 */
	@Override
	ListX<ListX<T>> grouped(int groupSize);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
	 */
	@Override 
	<K, A, D> ListX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function)
	 */
	@Override
	<K> ListX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable)
	 */
	@Override
	<U> ListX<Tuple2<T, U>> zip(Iterable<U> other);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	<U, R> ListX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int)
	 */
	@Override
	ListX<ListX<T>> sliding(int windowSize);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int, int)
	 */
	@Override
	ListX<ListX<T>> sliding(int windowSize, int increment);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(com.aol.cyclops.Monoid)
	 */
	@Override
	ListX<T> scanLeft(Monoid<T> monoid);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
	 */
	@Override
	<U> ListX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(com.aol.cyclops.Monoid)
	 */
	@Override
	ListX<T> scanRight(Monoid<T> monoid);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
	 */
	@Override
	<U> ListX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);
	
	/* Makes a defensive copy of this ListX replacing the value at i with the specified element
	 *  (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableSequenceX#with(int, java.lang.Object)
	 */
	@Override
	ListX<T> with(int i,T element);
	
	/* (non-Javadoc)
	 * @see java.util.List#subList(int, int)
	 */
	@Override
	public ListX<T> subList(int start, int end);
	
	/* Lazy operation
	 * 
	 * (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plus(java.lang.Object)
	 */
	@Override
	ListX<T> plus(T e);
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plusAll(java.util.Collection)
	 */
	@Override
	ListX<T> plusAll(Collection<? extends T> list);
	
	/* Eager operation
	 * 
	 * (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableSequenceX#minus(int)
	 */
	@Override
	ListX<T> minus(int pos);
	
	/* Lazy operation
	 * (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minus(java.lang.Object)
	 */
	ListX<T> minus(Object e);
	
	/* Lazy operation
	 * (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minusAll(java.util.Collection)
	 */
	ListX<T> minusAll(Collection<?> list);
	
	/* Eager operation
	 * (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableSequenceX#plus(int, java.lang.Object)
	 */
	@Override
	ListX<T> plus(int i, T e);
	
	
	/* Eageer operation
	 * (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableSequenceX#plusAll(int, java.util.Collection)
	 */
	@Override
	ListX<T> plusAll(int i, Collection<? extends T> list);

	@Override
	int size();

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.FluentCollectionX#plusInOrder(java.lang.Object)
	 */
	@Override
	default ListX<T> plusInOrder(T e) {
		
		return (ListX<T>)MutableSequenceX.super.plusInOrder(e);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#peek(java.util.function.Consumer)
	 */
	@Override
	default ListX<T> peek(Consumer<? super T> c) {
        return (ListX<T>)MutableCollectionX.super.peek(c);
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
	<U> ListX<Tuple2<T, U>> zipStream(Stream<U> other);

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	<U> ListX<Tuple2<T, U>> zip(Seq<U> other) ;

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	<S, U> ListX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	<T2, T3, T4> ListX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) ;

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zipWithIndex()
	 */
	@Override
	ListX<Tuple2<T, Long>> zipWithIndex();

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted()
	 */
	@Override
	ListX<T> sorted();

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	ListX<T> sorted(Comparator<? super T> c);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	ListX<T> skipWhile(Predicate<? super T> p);
	

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	ListX<T> skipUntil(Predicate<? super T> p);

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle()
	 */
	@Override
	ListX<T> shuffle();

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipLast(int)
	 */
	@Override
	ListX<T> skipLast(int num) ;

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
	 */
	@Override
	ListX<T> shuffle(Random random);

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#permutations()
	 */
	@Override
	ListX<ReactiveSeq<T>> permutations();

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations(int)
	 */
	@Override
	ListX<ReactiveSeq<T>> combinations(int size);

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations()
	 */
	@Override
	ListX<ReactiveSeq<T>> combinations();
	

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cast(java.lang.Class)
	 */
	@Override
	public <U> ListX<U> cast(Class<U> type);

	

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#distinct()
	 */
	@Override
	ListX<T> distinct();

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	ListX<T> limitWhile(Predicate<? super T> p);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	ListX<T> limitUntil(Predicate<? super T> p);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	ListX<T> intersperse(T value);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
	 */
	@Override
	ListX<T> limitLast(int num);
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	ListX<T> onEmpty(T value);
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	ListX<T> onEmptyGet(Supplier<T> supplier);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	<X extends Throwable> ListX<T> onEmptyThrow(Supplier<X> supplier);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
	 */
	@Override
	<U> ListX<U> ofType(Class<U> type);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	ListX<T> filterNot(Predicate<? super T> fn);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
	 */
	@Override
	ListX<T> notNull();

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	ListX<T> removeAll(Stream<T> stream);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	ListX<T> removeAll(Iterable<T> it);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	ListX<T> removeAll(T... values);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	ListX<T> retainAll(Iterable<T> it);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	ListX<T> retainAll(Stream<T> stream);

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	ListX<T> retainAll(T... values);

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int, java.util.function.Supplier)
	 */
	@Override
	 <C extends Collection<? super T>> ListX<C> grouped(int size, Supplier<C> supplier);

	 /* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate)
	 */
	@Override
	 ListX<ListX<T>> groupedUntil(Predicate<? super T> predicate);


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate)
	 */
	@Override
	 ListX<ListX<T>> groupedWhile(Predicate<? super T> predicate);


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
	 */
	@Override
	 <C extends Collection<? super T>> ListX<C> groupedWhile(Predicate<? super T> predicate,
	            Supplier<C> factory);


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
	 */
	@Override
	 <C extends Collection<? super T>> ListX<C> groupedUntil(Predicate<? super T> predicate,
	            Supplier<C> factory);


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedStatefullyWhile(java.util.function.BiPredicate)
	 */
	@Override
	ListX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate);
	    
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#removeAll(org.jooq.lambda.Seq)
	 */
	@Override
	ListX<T> removeAll(Seq<T> stream);


	 /* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#retainAll(org.jooq.lambda.Seq)
	 */
	@Override
	 ListX<T> retainAll(Seq<T> stream);
	
	
}
