package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Arrays;
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
import org.pcollections.HashTreePSet;
import org.pcollections.PSet;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public interface PSetX<T> extends PSet<T>, FluentCollectionX<T>{
	
	public static <T> PSetX<T> of(T...values){
		
		return new PSetXImpl<>(HashTreePSet.from(Arrays.asList(values)));
	}
	public static <T> PSetX<T> empty(){
		return new PSetXImpl<>(HashTreePSet .empty());
	}
	public static <T> PSetX<T> singleton(T value){
		return new PSetXImpl<>(HashTreePSet.singleton(value));
	}
	public static<T> PSetX<T> fromIterable(Iterable<T> iterable){
		if(iterable instanceof PSetX)
			return (PSetX)iterable;
		if(iterable instanceof PSet)
			return new PSetXImpl<>((PSet)(iterable));
		PSet<T> res = HashTreePSet.<T>empty();
		Iterator<T> it = iterable.iterator();
		while(it.hasNext())
			res = res.plus(it.next());
		
		return new PSetXImpl<>(res);
	}
    /**
     * Construct a PSetX from an Publisher
     * 
     * @param publisher
     *            to construct PSetX from
     * @return PSetX
     */
    public static <T> PSetX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toPSetX();
    }
	public static<T> PSetX<T> fromCollection(Collection<T> stream){
		if(stream instanceof PSetX)
			return (PSetX)(stream);
		if(stream instanceof PSet)
			return new PSetXImpl<>((PSet)(stream));
		return new PSetXImpl<>(HashTreePSet.from(stream));
	}
	public static<T> PSetX<T> fromStream(Stream<T> stream){
		return Reducers.<T>toPSetX().mapReduce(stream);
	}
	@Override
	default PSetX<T> toPSetX() {
		return this;
	}
	
	   /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
	   */
	  @Override
	  <R> PSetX<R> unit(Collection<R> col);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
	   */
	  @Override
	  <R> PSetX<R> unit(R value);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
	   */
	  @Override
	  <R> PSetX<R> unitIterator(Iterator<R> it);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#patternMatch(java.util.function.Function, java.util.function.Supplier)
	   */
	  @Override
	  <R> PSetX<R> patternMatch(
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
	  PSetX<T> reverse();
	  /**
	   * Combine two adjacent elements in a PSetX using the supplied BinaryOperator
	   * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
	   * with it's neighbor
	   * <pre>
	   * {@code 
	   *  PSetX.of(1,1,2,3)
	                 .combine((a, b)->a.equals(b),Semigroups.intSum)
	                 .toPSetX()
	                 
	   *  //PSetX(3,4) 
	   * }</pre>
	   * 
	   * @param predicate Test to see if two neighbors should be joined
	   * @param op Reducer to combine neighbors
	   * @return Combined / Partially Reduced PSetX
	   */
	  @Override
	  PSetX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filter(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> filter(Predicate<? super T> pred);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#map(java.util.function.Function)
	   */
	  @Override
	  <R> PSetX<R> map(Function<? super T, ? extends R> mapper);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#flatMap(java.util.function.Function)
	   */
	  @Override
	  <R> PSetX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limit(long)
	   */
	  @Override
	  PSetX<T> limit(long num);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#skip(long)
	   */
	  @Override
	  PSetX<T> skip(long num);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeRight(int)
	   */
	  @Override
	  PSetX<T> takeRight(int num);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#dropRight(int)
	   */
	  @Override
	  PSetX<T> dropRight(int num);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#takeWhile(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> takeWhile(Predicate<? super T> p);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropWhile(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> dropWhile(Predicate<? super T> p);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#takeUntil(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> takeUntil(Predicate<? super T> p);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#dropUntil(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> dropUntil(Predicate<? super T> p);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#trampoline(java.util.function.Function)
	   */
	  @Override
	  <R> PSetX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#slice(long, long)
	   */
	  @Override
	  PSetX<T> slice(long from, long to);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#sorted(java.util.function.Function)
	   */
	  @Override
	  <U extends Comparable<? super U>> PSetX<T> sorted(Function<? super T, ? extends U> function);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int)
	   */
	  @Override
	  PSetX<ListX<T>> grouped(int groupSize);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
	   */
	  @Override 
	  <K, A, D> PSetX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(java.util.function.Function)
	   */
	  @Override
	  <K> PSetX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable)
	   */
	  @Override
	  <U> PSetX<Tuple2<T, U>> zip(Iterable<U> other);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	   */
	  @Override
	  <U, R> PSetX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int)
	   */
	  @Override
	  PSetX<ListX<T>> sliding(int windowSize);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sliding(int, int)
	   */
	  @Override
	  PSetX<ListX<T>> sliding(int windowSize, int increment);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(com.aol.cyclops.Monoid)
	   */
	  @Override
	  PSetX<T> scanLeft(Monoid<T> monoid);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
	   */
	  @Override
	  <U> PSetX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(com.aol.cyclops.Monoid)
	   */
	  @Override
	  PSetX<T> scanRight(Monoid<T> monoid);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
	   */
	  @Override
	  <U> PSetX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);
	  

	  /* Lazy operation
	   * 
	   * (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plus(java.lang.Object)
	   */
	  @Override
	  PSetX<T> plus(T e);
	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#plusAll(java.util.Collection)
	   */
	  @Override
	  PSetX<T> plusAll(Collection<? extends T> list);
	  

	  
	  /*
	   * (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minus(java.lang.Object)
	   */
	  PSetX<T> minus(Object e);
	  
	  /* 
	   * (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#minusAll(java.util.Collection)
	   */
	  PSetX<T> minusAll(Collection<?> list);
	  


	  @Override
	  int size();

	 

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.CollectionX#peek(java.util.function.Consumer)
	   */
	  @Override
	  default PSetX<T> peek(Consumer<? super T> c) {
	      return (PSetX<T>)FluentCollectionX.super.peek(c);
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
	  <U> PSetX<Tuple2<T, U>> zipStream(Stream<U> other);

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip(org.jooq.lambda.Seq)
	   */
	  @Override
	  <U> PSetX<Tuple2<T, U>> zip(Seq<U> other) ;

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	   */
	  @Override
	  <S, U> PSetX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	   */
	  @Override
	  <T2, T3, T4> PSetX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
	          Stream<T4> fourth) ;

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#zipWithIndex()
	   */
	  @Override
	  PSetX<Tuple2<T, Long>> zipWithIndex();

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted()
	   */
	  @Override
	  PSetX<T> sorted();

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#sorted(java.util.Comparator)
	   */
	  @Override
	  PSetX<T> sorted(Comparator<? super T> c);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipWhile(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> skipWhile(Predicate<? super T> p);
	  

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipUntil(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> skipUntil(Predicate<? super T> p);

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle()
	   */
	  @Override
	  PSetX<T> shuffle();

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#skipLast(int)
	   */
	  @Override
	  PSetX<T> skipLast(int num) ;

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#shuffle(java.util.Random)
	   */
	  @Override
	  PSetX<T> shuffle(Random random);

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#permutations()
	   */
	  @Override
	  PSetX<ReactiveSeq<T>> permutations();

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations(int)
	   */
	  @Override
	  PSetX<ReactiveSeq<T>> combinations(int size);

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#combinations()
	   */
	  @Override
	  PSetX<ReactiveSeq<T>> combinations();
	  

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#cast(java.lang.Class)
	   */
	  @Override
	  public <U> PSetX<U> cast(Class<U> type);

	  

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#distinct()
	   */
	  @Override
	  PSetX<T> distinct();

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitWhile(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> limitWhile(Predicate<? super T> p);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitUntil(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> limitUntil(Predicate<? super T> p);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#intersperse(java.lang.Object)
	   */
	  @Override
	  PSetX<T> intersperse(T value);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#limitLast(int)
	   */
	  @Override
	  PSetX<T> limitLast(int num);
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmpty(java.lang.Object)
	   */
	  @Override
	  PSetX<T> onEmpty(T value);
	  

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyGet(java.util.function.Supplier)
	   */
	  @Override
	  PSetX<T> onEmptyGet(Supplier<T> supplier);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#onEmptyThrow(java.util.function.Supplier)
	   */
	  @Override
	  <X extends Throwable> PSetX<T> onEmptyThrow(Supplier<X> supplier);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#ofType(java.lang.Class)
	   */
	  @Override
	  <U> PSetX<U> ofType(Class<U> type);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#filterNot(java.util.function.Predicate)
	   */
	  @Override
	  PSetX<T> filterNot(Predicate<? super T> fn);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#notNull()
	   */
	  @Override
	  PSetX<T> notNull();

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.util.stream.Stream)
	   */
	  @Override
	  PSetX<T> removeAll(Stream<T> stream);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Iterable)
	   */
	  @Override
	  PSetX<T> removeAll(Iterable<T> it);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#removeAll(java.lang.Object[])
	   */
	  @Override
	  PSetX<T> removeAll(T... values);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Iterable)
	   */
	  @Override
	  PSetX<T> retainAll(Iterable<T> it);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.util.stream.Stream)
	   */
	  @Override
	  PSetX<T> retainAll(Stream<T> stream);

	  /* (non-Javadoc)
	   * @see com.aol.cyclops.collections.extensions.standard.MutableCollectionX#retainAll(java.lang.Object[])
	   */
	  @Override
	  PSetX<T> retainAll(T... values);

	  
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#grouped(int, java.util.function.Supplier)
	   */
	  @Override
	   <C extends Collection<? super T>> PSetX<C> grouped(int size, Supplier<C> supplier);

	   /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate)
	   */
	  @Override
	   PSetX<ListX<T>> groupedUntil(Predicate<? super T> predicate);


	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate)
	   */
	  @Override
	   PSetX<ListX<T>> groupedWhile(Predicate<? super T> predicate);


	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
	   */
	  @Override
	   <C extends Collection<? super T>> PSetX<C> groupedWhile(Predicate<? super T> predicate,
	              Supplier<C> factory);


	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
	   */
	  @Override
	   <C extends Collection<? super T>> PSetX<C> groupedUntil(Predicate<? super T> predicate,
	              Supplier<C> factory);


	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#groupedStatefullyWhile(java.util.function.BiPredicate)
	   */
	  @Override
	  PSetX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate);
	      
	  /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#removeAll(org.jooq.lambda.Seq)
	   */
	  @Override
	  PSetX<T> removeAll(Seq<T> stream);


	   /* (non-Javadoc)
	   * @see com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX#retainAll(org.jooq.lambda.Seq)
	   */
	  @Override
	   PSetX<T> retainAll(Seq<T> stream);
}
