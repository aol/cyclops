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
import org.pcollections.PVector;
import org.pcollections.TreePVector;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public interface PVectorX<T> extends PVector<T>, FluentCollectionX<T>{
	
	/**
	 * Construct a PVector from the provided values 
	 * 
	 * <pre>
	 * {@code 
	 *  List<String> list = PVectors.of("a","b","c");
	 *  
	 *  // or
	 *  
	 *  PVector<String> list = PVectors.of("a","b","c");
	 *  
	 *  
	 * }
	 * </pre>
	 * 
	 * 
	 * @param values To add to PVector
	 * @return new PVector
	 */
	public static <T> PVectorX<T> of(T...values){
		return new PVectorXImpl<>(TreePVector.from(Arrays.asList(values)));
	}
	/**
	 * <pre>
	 * {@code 
	 *     List<String> empty = PVectors.empty();
	 *    //or
	 *    
	 *     PVector<String> empty = PVectors.empty();
	 * }
	 * </pre>
	 * @return an empty PVector
	 */
	public static<T> PVectorX<T> empty(){
		return new PVectorXImpl<>(TreePVector .empty());
	}
	/**
	 * Construct a PVector containing a single value
	 * </pre>
	 * {@code 
	 *    List<String> single = PVectors.singleton("1");
	 *    
	 *    //or
	 *    
	 *    PVector<String> single = PVectors.singleton("1");
	 * 
	 * }
	 * </pre>
	 * 
	 * @param value Single value for PVector
	 * @return PVector with a single value
	 */
	public static <T> PVectorX<T> singleton(T value){
		return new PVectorXImpl<>(TreePVector.singleton(value));
	}
    /**
     * Construct a PVectorX from an Publisher
     * 
     * @param publisher
     *            to construct PVectorX from
     * @return PVectorX
     */
    public static <T> PVectorX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toPVectorX();
    }
	public static<T> PVectorX<T> fromIterable(Iterable<T> iterable){
		if(iterable instanceof PVectorX)
			return (PVectorX)iterable;
		if(iterable instanceof PVector)
			return new PVectorXImpl<>((PVector)(iterable));
		PVector<T> res = TreePVector.<T>empty();
		Iterator<T> it = iterable.iterator();
		while(it.hasNext())
			res = res.plus(it.next());
		
		return new PVectorXImpl<>(res);
	}
	/**
	 * Create a PVector from the supplied Colleciton
	 * <pre>
	 * {@code 
	 *   PVector<Integer> list = PVectors.fromCollection(Arrays.asList(1,2,3));
	 *   
	 * }
	 * </pre>
	 * 
	 * @param values to add to new PVector
	 * @return PVector containing values
	 */
	public static <T> PVectorX<T> fromCollection(Collection<T> values){
		if(values instanceof PVectorX)
			return (PVectorX)values;
		if(values instanceof PVector)
			return new PVectorXImpl<>((PVector)values);
		return new PVectorXImpl<>(TreePVector.from(values));
	}
	/**
	 * Reduce (immutable Collection) a Stream to a PVector
	 * 
	 * <pre>
	 * {@code 
	 *    PVector<Integer> list = PVectors.fromStream(Stream.of(1,2,3));
	 * 
	 *  //list = [1,2,3]
	 * }</pre>
	 * 
	 * 
	 * @param stream to convert to a PVector
	 * @return
	 */
	public static<T> PVectorX<T> fromStream(Stream<T> stream){
		return Reducers.<T>toPVectorX().mapReduce(stream);
	}

	
	default PVector<T> toPVector(){
		return this;
	}
	
	default <X> PVectorX<X> from(Collection<X> col){
		return fromCollection(col);
	}
	default <T> Reducer<PVector<T>> monoid(){
		return Reducers.toPVector();
	}
	@Override
	default PVectorX<T> toPVectorX() {
		return this;
	}
	
	
	 /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#unit(java.util.Collection)
     */
    @Override
    <R> PVectorX<R> unit(Collection<R> col);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.Unit#unit(java.lang.Object)
     */
    @Override
    <R> PVectorX<R> unit(R value);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.types.IterableFunctor#unitIterator(java.util.Iterator)
     */
    @Override
    <R> PVectorX<R> unitIterator(Iterator<R> it);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#patternMatch(java.util.function.Function, java.util.function.Supplier)
     */
    @Override
    <R> PVectorX<R> patternMatch(
            Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise);
    
    /* (non-Javadoc)
     * @see java.util.Collection#stream()
     */
    @Override
    ReactiveSeq<T> stream();


    
    
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#reverse()
     */
    @Override
    PVectorX<T> reverse();
    /**
     * Combine two adjacent elements in a PVectorX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  PVectorX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toPVectorX()
                   
     *  //PVectorX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced PVectorX
     */
    @Override
    PVectorX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op);
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#filter(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> filter(Predicate<? super T> pred);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#map(java.util.function.Function)
     */
    @Override
    <R> PVectorX<R> map(Function<? super T, ? extends R> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#flatMap(java.util.function.Function)
     */
    @Override
    <R> PVectorX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#limit(long)
     */
    @Override
    PVectorX<T> limit(long num);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#skip(long)
     */
    @Override
    PVectorX<T> skip(long num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#takeRight(int)
     */
    @Override
    PVectorX<T> takeRight(int num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#dropRight(int)
     */
    @Override
    PVectorX<T> dropRight(int num);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#takeWhile(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> takeWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#dropWhile(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> dropWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#takeUntil(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> takeUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#dropUntil(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> dropUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#trampoline(java.util.function.Function)
     */
    @Override
    <R> PVectorX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#slice(long, long)
     */
    @Override
    PVectorX<T> slice(long from, long to);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#sorted(java.util.function.Function)
     */
    @Override
    <U extends Comparable<? super U>> PVectorX<T> sorted(Function<? super T, ? extends U> function);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#grouped(int)
     */
    @Override
    PVectorX<ListX<T>> grouped(int groupSize);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
     */
    @Override 
    <K, A, D> PVectorX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#grouped(java.util.function.Function)
     */
    @Override
    <K> PVectorX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zip(java.lang.Iterable)
     */
    @Override
    <U> PVectorX<Tuple2<T, U>> zip(Iterable<U> other);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
     */
    @Override
    <U, R> PVectorX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#sliding(int)
     */
    @Override
    PVectorX<ListX<T>> sliding(int windowSize);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#sliding(int, int)
     */
    @Override
    PVectorX<ListX<T>> sliding(int windowSize, int increment);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#scanLeft(com.aol.cyclops.Monoid)
     */
    @Override
    PVectorX<T> scanLeft(Monoid<T> monoid);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> PVectorX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#scanRight(com.aol.cyclops.Monoid)
     */
    @Override
    PVectorX<T> scanRight(Monoid<T> monoid);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
     */
    @Override
    <U> PVectorX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner);
    

    /* Lazy operation
     * 
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#plus(java.lang.Object)
     */
    @Override
    PVectorX<T> plus(T e);
    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#plusAll(java.util.Collection)
     */
    @Override
    PVectorX<T> plusAll(Collection<? extends T> list);
    

    
    /*
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#minus(java.lang.Object)
     */
    PVectorX<T> minus(Object e);
    
    /* 
     * (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#minusAll(java.util.Collection)
     */
    PVectorX<T> minusAll(Collection<?> list);
    


    @Override
    int size();

   

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#peek(java.util.function.Consumer)
     */
    @Override
    default PVectorX<T> peek(Consumer<? super T> c) {
        return (PVectorX<T>)FluentCollectionX.super.peek(c);
    }
    



    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#cycle(int)
     */
    @Override
    PVectorX<T> cycle(int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#cycle(com.aol.cyclops.Monoid, int)
     */
    @Override
    PVectorX<T> cycle(Monoid<T> m, int times);

    /* (non-Javadoc)
     * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> cycleWhile(Predicate<? super T> predicate);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#cycleUntil(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> cycleUntil(Predicate<? super T> predicate) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zipStream(java.util.stream.Stream)
     */
    @Override
    <U> PVectorX<Tuple2<T, U>> zipStream(Stream<U> other);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zip(org.jooq.lambda.Seq)
     */
    @Override
    <U> PVectorX<Tuple2<T, U>> zip(Seq<U> other) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <S, U> PVectorX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
     */
    @Override
    <T2, T3, T4> PVectorX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
            Stream<T4> fourth) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#zipWithIndex()
     */
    @Override
    PVectorX<Tuple2<T, Long>> zipWithIndex();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#sorted()
     */
    @Override
    PVectorX<T> sorted();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#sorted(java.util.Comparator)
     */
    @Override
    PVectorX<T> sorted(Comparator<? super T> c);

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#skipWhile(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> skipWhile(Predicate<? super T> p);
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#skipUntil(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> skipUntil(Predicate<? super T> p);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#shuffle()
     */
    @Override
    PVectorX<T> shuffle();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#skipLast(int)
     */
    @Override
    PVectorX<T> skipLast(int num) ;

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#shuffle(java.util.Random)
     */
    @Override
    PVectorX<T> shuffle(Random random);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#permutations()
     */
    @Override
    PVectorX<ReactiveSeq<T>> permutations();

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#combinations(int)
     */
    @Override
    PVectorX<ReactiveSeq<T>> combinations(int size);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#combinations()
     */
    @Override
    PVectorX<ReactiveSeq<T>> combinations();
    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#cast(java.lang.Class)
     */
    @Override
    public <U> PVectorX<U> cast(Class<U> type);

    

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#distinct()
     */
    @Override
    PVectorX<T> distinct();

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#limitWhile(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> limitWhile(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#limitUntil(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> limitUntil(Predicate<? super T> p);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#intersperse(java.lang.Object)
     */
    @Override
    PVectorX<T> intersperse(T value);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#limitLast(int)
     */
    @Override
    PVectorX<T> limitLast(int num);
    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#onEmpty(java.lang.Object)
     */
    @Override
    PVectorX<T> onEmpty(T value);
    

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#onEmptyGet(java.util.function.Supplier)
     */
    @Override
    PVectorX<T> onEmptyGet(Supplier<T> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#onEmptyThrow(java.util.function.Supplier)
     */
    @Override
    <X extends Throwable> PVectorX<T> onEmptyThrow(Supplier<X> supplier);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#ofType(java.lang.Class)
     */
    @Override
    <U> PVectorX<U> ofType(Class<U> type);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#filterNot(java.util.function.Predicate)
     */
    @Override
    PVectorX<T> filterNot(Predicate<? super T> fn);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#notNull()
     */
    @Override
    PVectorX<T> notNull();

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#removeAll(java.util.stream.Stream)
     */
    @Override
    PVectorX<T> removeAll(Stream<T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#removeAll(java.lang.Iterable)
     */
    @Override
    PVectorX<T> removeAll(Iterable<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#removeAll(java.lang.Object[])
     */
    @Override
    PVectorX<T> removeAll(T... values);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#retainAll(java.lang.Iterable)
     */
    @Override
    PVectorX<T> retainAll(Iterable<T> it);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#retainAll(java.util.stream.Stream)
     */
    @Override
    PVectorX<T> retainAll(Stream<T> stream);

    /* (non-Javadoc)
     * @see com.aol.cyclops.collections.extensions.FluentCollectionX#retainAll(java.lang.Object[])
     */
    @Override
    PVectorX<T> retainAll(T... values);

    
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> PVectorX<C> grouped(int size, Supplier<C> supplier);

     /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
     PVectorX<ListX<T>> groupedUntil(Predicate<? super T> predicate);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
     PVectorX<ListX<T>> groupedWhile(Predicate<? super T> predicate);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> PVectorX<C> groupedWhile(Predicate<? super T> predicate,
                Supplier<C> factory);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
     <C extends Collection<? super T>> PVectorX<C> groupedUntil(Predicate<? super T> predicate,
                Supplier<C> factory);


    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    PVectorX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate);
        
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#removeAll(org.jooq.lambda.Seq)
     */
    @Override
    PVectorX<T> removeAll(Seq<T> stream);


     /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#retainAll(org.jooq.lambda.Seq)
     */
    @Override
     PVectorX<T> retainAll(Seq<T> stream);
	    
}
