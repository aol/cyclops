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
import org.pcollections.HashTreePBag;
import org.pcollections.MapPBag;
import org.pcollections.PBag;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public interface PBagX<T> extends PBag<T>, FluentCollectionX<T>{
	ReactiveSeq<T> stream();
	

	public static<T> PBagX<T> of(T...values){
		return new PBagXImpl<>(HashTreePBag.from(Arrays.asList(values)));
	}
	
	public static<T> PBagX<T> empty(){
		return new PBagXImpl<>(HashTreePBag .empty());
	}
	public static<T> PBagX<T> singleton(T value){
		return new PBagXImpl<>(HashTreePBag.singleton(value));
	}
    /**
     * Construct a PBagX from an Publisher
     * 
     * @param publisher
     *            to construct PBagX from
     * @return PBagX
     */
    public static <T> PBagX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toPBagX();
    }
	public static<T> PBagX<T> fromIterable(Iterable<T> iterable){
		if(iterable instanceof PBagX)
			return (PBagX)iterable;
		if(iterable instanceof PBag)
			return new PBagXImpl<>((PBag)(iterable));
		MapPBag<T> res = HashTreePBag.<T>empty();
		Iterator<T> it = iterable.iterator();
		while(it.hasNext())
			res = res.plus(it.next());
		
		return new PBagXImpl<>(res);
	}
	public static<T> PBagX<T> fromCollection(Collection<T> stream){
		if(stream instanceof PBagX)
			return (PBagX)stream;
		if(stream instanceof PBag)
			return new PBagXImpl<>((PBag)(stream));
		
		return new PBagXImpl<>(HashTreePBag.from(stream));
	}
	public static<T> PBagX<T> fromStream(Stream<T> stream){
		return Reducers.<T>toPBagX().mapReduce(stream);
	}
	
	  /**
     * Combine two adjacent elements in a PBagX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  PBagX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced PBagX
     */
    PBagX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);
	/* (non-Javadoc)
	 * @see com.aol.cyclops.sequence.traits.ConvertableSequence#toListX()
	 */
	@Override
	default PBagX<T> toPBagX() {
		return this;
	}
	
	default PBag<T> toPBag(){
		return this;
	}
	
	default <X> PBagX<X> from(Collection<X> col){
		return fromCollection(col);
	}
	default <T> Reducer<PBag<T>> monoid(){
		return Reducers.toPBag();
	}
	
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plus(java.lang.Object)
	 */
	@Override
	public PBagX<T> plus(T e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#plusAll(java.util.Collection)
	 */
	@Override
	public PBagX<T> plusAll(Collection<? extends T> list) ;
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minus(java.lang.Object)
	 */
	@Override
	public PBagX<T> minus(Object e);
	/* (non-Javadoc)
	 * @see org.pcollections.PSet#minusAll(java.util.Collection)
	 */
	@Override
	public PBagX<T> minusAll(Collection<?> list);

	

    /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
    */
   @Override
   <R> PBagX<R> unit(Collection<R> col);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
    */
   @Override
   <R> PBagX<R> unit(R value);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
    */
   @Override
   <R> PBagX<R> unitIterator(Iterator<R> it);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#patternMatch(java.util.function.Function, java.util.function.Supplier)
    */
   @Override
   <R> PBagX<R> patternMatch(
           Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise);
   
  
 

   
   
   

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#reverse()
    */
   @Override
   PBagX<T> reverse();
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#filter(java.util.function.Predicate)
    */
   @Override
   PBagX<T> filter(Predicate<? super T> pred);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#map(java.util.function.Function)
    */
   @Override
   <R> PBagX<R> map(Function<? super T, ? extends R> mapper);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#flatMap(java.util.function.Function)
    */
   @Override
   <R> PBagX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#limit(long)
    */
   @Override
   PBagX<T> limit(long num);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#skip(long)
    */
   @Override
   PBagX<T> skip(long num);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#takeRight(int)
    */
   @Override
   PBagX<T> takeRight(int num);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#dropRight(int)
    */
   @Override
   PBagX<T> dropRight(int num);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#takeWhile(java.util.function.Predicate)
    */
   @Override
   PBagX<T> takeWhile(Predicate<? super T> p);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#dropWhile(java.util.function.Predicate)
    */
   @Override
   PBagX<T> dropWhile(Predicate<? super T> p);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#takeUntil(java.util.function.Predicate)
    */
   @Override
   PBagX<T> takeUntil(Predicate<? super T> p);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#dropUntil(java.util.function.Predicate)
    */
   @Override
   PBagX<T> dropUntil(Predicate<? super T> p);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#trampoline(java.util.function.Function)
    */
   @Override
   <R> PBagX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#slice(long, long)
    */
   @Override
   PBagX<T> slice(long from, long to);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#sorted(java.util.function.Function)
    */
   @Override
   <U extends Comparable<? super U>> PBagX<T> sorted(Function<? super T, ? extends U> function);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#grouped(int)
    */
   @Override
   PBagX<ListX<T>> grouped(int groupSize);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
    */
   @Override 
   <K, A, D> PBagX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#grouped(java.util.function.Function)
    */
   @Override
   <K> PBagX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zip(java.lang.Iterable)
    */
   @Override
   <U> PBagX<Tuple2<T, U>> zip(Iterable<U> other);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
    */
   @Override
   <U, R> PBagX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#sliding(int)
    */
   @Override
   PBagX<ListX<T>> sliding(int windowSize);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#sliding(int, int)
    */
   @Override
   PBagX<ListX<T>> sliding(int windowSize, int increment);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#scanLeft(com.aol.cyclops.Monoid)
    */
   @Override
   PBagX<T> scanLeft(Monoid<T> monoid);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
    */
   @Override
   <U> PBagX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#scanRight(com.aol.cyclops.Monoid)
    */
   @Override
   PBagX<T> scanRight(Monoid<T> monoid);
   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
    */
   @Override
   <U> PBagX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);
   




   @Override
   int size();

  

   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.CollectionX#peek(java.util.function.Consumer)
    */
   @Override
   default PBagX<T> peek(Consumer<? super T> c) {
       return (PBagX<T>)FluentCollectionX.super.peek(c);
   }
   



   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#cycle(int)
    */
   @Override
   PBagX<T> cycle(int times);

   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#cycle(com.aol.cyclops.Monoid, int)
    */
   @Override
   PBagX<T> cycle(Monoid<T> m, int times);

   /* (non-Javadoc)
    * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
    */
   @Override
   PBagX<T> cycleWhile(Predicate<? super T> predicate);

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#cycleUntil(java.util.function.Predicate)
    */
   @Override
   PBagX<T> cycleUntil(Predicate<? super T> predicate) ;

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zipStream(java.util.stream.Stream)
    */
   @Override
   <U> PBagX<Tuple2<T, U>> zipStream(Stream<U> other);

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zip(org.jooq.lambda.Seq)
    */
   @Override
   <U> PBagX<Tuple2<T, U>> zip(Seq<U> other) ;

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
    */
   @Override
   <S, U> PBagX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
    */
   @Override
   <T2, T3, T4> PBagX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
           Stream<T4> fourth) ;

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zipWithIndex()
    */
   @Override
   PBagX<Tuple2<T, Long>> zipWithIndex();

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#sorted()
    */
   @Override
   PBagX<T> sorted();

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#sorted(java.util.Comparator)
    */
   @Override
   PBagX<T> sorted(Comparator<? super T> c);

   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#skipWhile(java.util.function.Predicate)
    */
   @Override
   PBagX<T> skipWhile(Predicate<? super T> p);
   

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#skipUntil(java.util.function.Predicate)
    */
   @Override
   PBagX<T> skipUntil(Predicate<? super T> p);

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#shuffle()
    */
   @Override
   PBagX<T> shuffle();

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#skipLast(int)
    */
   @Override
   PBagX<T> skipLast(int num) ;

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#shuffle(java.util.Random)
    */
   @Override
   PBagX<T> shuffle(Random random);

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#permutations()
    */
   @Override
   PBagX<ReactiveSeq<T>> permutations();

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#combinations(int)
    */
   @Override
   PBagX<ReactiveSeq<T>> combinations(int size);

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#combinations()
    */
   @Override
   PBagX<ReactiveSeq<T>> combinations();
   

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#cast(java.lang.Class)
    */
   @Override
   public <U> PBagX<U> cast(Class<U> type);

   

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#distinct()
    */
   @Override
   PBagX<T> distinct();

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#limitWhile(java.util.function.Predicate)
    */
   @Override
   PBagX<T> limitWhile(Predicate<? super T> p);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#limitUntil(java.util.function.Predicate)
    */
   @Override
   PBagX<T> limitUntil(Predicate<? super T> p);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#intersperse(java.lang.Object)
    */
   @Override
   PBagX<T> intersperse(T value);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#limitLast(int)
    */
   @Override
   PBagX<T> limitLast(int num);
   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#onEmpty(java.lang.Object)
    */
   @Override
   PBagX<T> onEmpty(T value);
   

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#onEmptyGet(java.util.function.Supplier)
    */
   @Override
   PBagX<T> onEmptyGet(Supplier<T> supplier);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#onEmptyThrow(java.util.function.Supplier)
    */
   @Override
   <X extends Throwable> PBagX<T> onEmptyThrow(Supplier<X> supplier);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#ofType(java.lang.Class)
    */
   @Override
   <U> PBagX<U> ofType(Class<U> type);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#filterNot(java.util.function.Predicate)
    */
   @Override
   PBagX<T> filterNot(Predicate<? super T> fn);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#notNull()
    */
   @Override
   PBagX<T> notNull();

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#removeAll(java.util.stream.Stream)
    */
   @Override
   PBagX<T> removeAll(Stream<T> stream);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#removeAll(java.lang.Iterable)
    */
   @Override
   PBagX<T> removeAll(Iterable<T> it);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#removeAll(java.lang.Object[])
    */
   @Override
   PBagX<T> removeAll(T... values);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#retainAll(java.lang.Iterable)
    */
   @Override
   PBagX<T> retainAll(Iterable<T> it);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#retainAll(java.util.stream.Stream)
    */
   @Override
   PBagX<T> retainAll(Stream<T> stream);

   /* (non-Javadoc)
    * @see com.aol.cyclops.collections.extensions.FluentCollectionX#retainAll(java.lang.Object[])
    */
   @Override
   PBagX<T> retainAll(T... values);

   
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#grouped(int, java.util.function.Supplier)
    */
   @Override
    <C extends Collection<? super T>> PBagX<C> grouped(int size, Supplier<C> supplier);

    /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedUntil(java.util.function.Predicate)
    */
   @Override
    PBagX<ListX<T>> groupedUntil(Predicate<? super T> predicate);


   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedWhile(java.util.function.Predicate)
    */
   @Override
    PBagX<ListX<T>> groupedWhile(Predicate<? super T> predicate);


   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
    */
   @Override
    <C extends Collection<? super T>> PBagX<C> groupedWhile(Predicate<? super T> predicate,
               Supplier<C> factory);


   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
    */
   @Override
    <C extends Collection<? super T>> PBagX<C> groupedUntil(Predicate<? super T> predicate,
               Supplier<C> factory);


   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedStatefullyWhile(java.util.function.BiPredicate)
    */
   @Override
   PBagX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate);
       
   /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#removeAll(org.jooq.lambda.Seq)
    */
   @Override
   PBagX<T> removeAll(Seq<T> stream);


    /* (non-Javadoc)
    * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#retainAll(org.jooq.lambda.Seq)
    */
   @Override
    PBagX<T> retainAll(Seq<T> stream);
}
