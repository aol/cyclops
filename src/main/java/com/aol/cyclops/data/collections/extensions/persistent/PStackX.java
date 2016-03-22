package com.aol.cyclops.data.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;
import java.util.function.BiFunction;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;
import org.reactivestreams.Publisher;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.Reducers;
import com.aol.cyclops.control.Matchable.CheckValues;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.FluentSequenceX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;

public interface PStackX<T> extends PStack<T>, PersistentCollectionX<T>, FluentSequenceX<T>{
	
	
	/**
	 * Construct a PStack from the provided values 
	 * 
	 * <pre>
	 * {@code 
	 *  List<String> list = PStacks.of("a","b","c");
	 *  
	 *  // or
	 *  
	 *  PStack<String> list = PStacks.of("a","b","c");
	 *  
	 *  
	 * }
	 * </pre>
	 * 
	 * 
	 * @param values To add to PStack
	 * @return new PStack
	 */
	public static <T> PStackX<T> of(T...values){
		return new PStackXImpl<>(ConsPStack.from(Arrays.asList(values)),true);
	}
    /**
     * Construct a PStackX from an Publisher
     * 
     * @param publisher
     *            to construct PStackX from
     * @return PStackX
     */
    public static <T> PStackX<T> fromPublisher(Publisher<? extends T> publisher) {
        return ReactiveSeq.fromPublisher((Publisher<T>)publisher).toPStackX();
    }
	public static<T> PStackX<T> fromIterable(Iterable<T> iterable){
		if(iterable instanceof PStackX)
			return (PStackX)iterable;
		if(iterable instanceof PStack)
			return new PStackXImpl<>((PStack)(iterable),true);
		PStack<T> res = ConsPStack.<T>empty();
		Iterator<T> it = iterable.iterator();
		while(it.hasNext())
			res = res.plus(it.next());
		
		return new PStackXImpl<>(res,true);
	}
	/**
	 * <pre>
	 * {@code 
	 *  List<String> list = PStacks.of(Arrays.asList("a","b","c"));
	 *  
	 *  // or
	 *  
	 *  PStack<String> list = PStacks.of(Arrays.asList("a","b","c"));
	 *  
	 *  
	 * }
	 * 
	 * @param values To add to PStack
	 * @return
	 */
	public static <T> PStackX<T> fromCollection(Collection<T> values){
		if(values instanceof PStackX)
			return (PStackX)values;
		if(values instanceof PStack)
			return new PStackXImpl<>((PStack)values,true);
		return new PStackXImpl<>(ConsPStack.from(values),true);
	}
	/**
	 * <pre>
	 * {@code 
	 *     List<String> empty = PStack.empty();
	 *    //or
	 *    
	 *     PStack<String> empty = PStack.empty();
	 * }
	 * </pre>
	 * @return an empty PStack
	 */
	public static <T> PStackX<T> empty(){
		return new PStackXImpl<>(ConsPStack.empty(),true);
	}
	/**
	 * Construct a PStack containing a single value
	 * </pre>
	 * {@code 
	 *    List<String> single = PStacks.singleton("1");
	 *    
	 *    //or
	 *    
	 *    PStack<String> single = PStacks.singleton("1");
	 * 
	 * }
	 * </pre>
	 * 
	 * @param value Single value for PVector
	 * @return PVector with a single value
	 */
	public static <T> PStackX<T> singleton(T value){
		return new PStackXImpl<>(ConsPStack.singleton(value),true);
	}
	/**
	 * Reduce (immutable Collection) a Stream to a PStack, note for efficiency reasons,
	 * the produced PStack is reversed.
	 * 
	 * 
	 * <pre>
	 * {@code 
	 *    PStack<Integer> list = PStacks.fromStream(Stream.of(1,2,3));
	 * 
	 *  //list = [3,2,1]
	 * }</pre>
	 * 
	 * 
	 * @param stream to convert to a PVector
	 * @return
	 */
	public static<T> PStackX<T> fromStream(Stream<T> stream){
	   	return Reducers.<T>toPStackX().mapReduce(stream)
		                            .efficientOpsOff();
	}
	@Override
	default PStackX<T> toPStackX() {
		return this;
	}
	
	  /**
     * Combine two adjacent elements in a PStackX using the supplied BinaryOperator
     * This is a stateful grouping & reduction operation. The output of a combination may in turn be combined
     * with it's neighbor
     * <pre>
     * {@code 
     *  PStackX.of(1,1,2,3)
                   .combine((a, b)->a.equals(b),Semigroups.intSum)
                   .toListX()
                   
     *  //ListX(3,4) 
     * }</pre>
     * 
     * @param predicate Test to see if two neighbors should be joined
     * @param op Reducer to combine neighbors
     * @return Combined / Partially Reduced PStackX
     */
    default PStackX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op){
        return (PStackX<T>)PersistentCollectionX.super.combine(predicate,op);
    }
    
	@Override
	default<R> PStackX<R> unit(Collection<R> col){
		if(isEfficientOps())
			return fromCollection(col);
		return fromCollection(col).efficientOpsOff();
	}
	@Override
	default <R> PStackX<R> unit(R value){
		return singleton(value);
	}
	@Override
	default <R> PStackX<R> unitIterator(Iterator<R> it){
		return fromIterable(()->it);
	}
	@Override
	default<R> PStackX<R> emptyUnit(){
		if(isEfficientOps())
			return empty();
		return PStackX.<R>empty().efficientOpsOff();
	}
	default PStack<T> toPStack(){
		return this;
	}
	@Override
	default PStackX<T> plusInOrder(T e){
		if(isEfficientOps())
			return plus(e);
		return plus(size(),e);
	}
	@Override
	ReactiveSeq<T> stream();
	default <X> PStackX<X> from(Collection<X> col){
		if(isEfficientOps())
			return fromCollection(col);
		return fromCollection(col).efficientOpsOff();
	}
	default <T> Reducer<PStack<T>> monoid(){
		if(isEfficientOps())
			return Reducers.toPStackReversed();
		return Reducers.toPStack();
		
	}
	


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#reverse()
	 */
	@Override
	default PStackX<T> reverse() {
		PStack<T> reversed = ConsPStack.empty();
		Iterator<T> it = iterator();
		while(it.hasNext())
			reversed = reversed.plus(0, it.next());
		return fromCollection(reversed);
	}
	

	
	PStackX<T> efficientOpsOn();
	PStackX<T> efficientOpsOff();
	boolean isEfficientOps();
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> filter(Predicate<? super T> pred) {
		return (PStackX<T>)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> PStackX<R> map(Function<? super T, ? extends R> mapper) {
		return (PStackX<R>)PersistentCollectionX.super.map(mapper);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> PStackX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper) {
		
		return (PStackX)PersistentCollectionX.super.flatMap(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limit(long)
	 */
	@Override
	default PStackX<T> limit(long num) {
		
		return (PStackX)PersistentCollectionX.super.limit(num);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skip(long)
	 */
	@Override
	default PStackX<T> skip(long num) {
		
		return (PStackX)PersistentCollectionX.super.skip(num);
	}
	default PStackX<T> takeRight(int num){
		return (PStackX<T>)PersistentCollectionX.super.takeRight(num);
	}
	default PStackX<T> dropRight(int num){
		return (PStackX<T>)PersistentCollectionX.super.dropRight(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> takeWhile(Predicate<? super T> p) {
		
		return (PStackX)PersistentCollectionX.super.takeWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> dropWhile(Predicate<? super T> p) {
		
		return (PStackX)PersistentCollectionX.super.dropWhile(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> takeUntil(Predicate<? super T> p) {
		
		return (PStackX)PersistentCollectionX.super.takeUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> dropUntil(Predicate<? super T> p) {
		return (PStackX)PersistentCollectionX.super.dropUntil(p);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#trampoline(java.util.function.Function)
	 */
	@Override
	default <R> PStackX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper) {
		
		return (PStackX)PersistentCollectionX.super.trampoline(mapper);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#slice(long, long)
	 */
	@Override
	default PStackX<T> slice(long from, long to) {
		return (PStackX)PersistentCollectionX.super.slice(from, to);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.function.Function)
	 */
	@Override
	default <U extends Comparable<? super U>> PStackX<T> sorted(Function<? super T, ? extends U> function) {
		return (PStackX)PersistentCollectionX.super.sorted(function);
	}
	public PStackX<T> minusAll(Collection<?> list);
	public PStackX<T> minus(Object remove);
	
	/**
	 * @param i
	 * @param e
	 * @return
	 * @see org.pcollections.PStack#with(int, java.lang.Object)
	 */
	public PStackX<T> with(int i, T e) ;

	/**
	 * @param i
	 * @param e
	 * @return
	 * @see org.pcollections.PStack#plus(int, java.lang.Object)
	 */
	public PStackX<T> plus(int i, T e);
	public PStackX<T> plus(T e);
	public PStackX<T> plusAll(Collection<? extends T> list) ;
	/**
	 * @param i
	 * @param list
	 * @return
	 * @see org.pcollections.PStack#plusAll(int, java.util.Collection)
	 */
	public PStackX<T> plusAll(int i, Collection<? extends T> list) ;

	/**
	 * @param i
	 * @return
	 * @see org.pcollections.PStack#minus(int)
	 */
	public PStackX<T> minus(int i);
	
	public PStackX<T> subList(int start, int end);
	
	default PStackX<ListX<T>> grouped(int groupSize){
		return  (PStackX<ListX<T>>)PersistentCollectionX.super.grouped(groupSize);
	}
	default <K, A, D> PStackX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return  (PStackX)PersistentCollectionX.super.grouped(classifier,downstream);
	}
	default <K> PStackX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return  (PStackX)PersistentCollectionX.super.grouped(classifier);
	}
	default <U> PStackX<Tuple2<T, U>> zip(Iterable<U> other){
		return  (PStackX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> PStackX<R> zip(Iterable<U> other,
			BiFunction<? super T, ? super U, ? extends R> zipper) {
		
		return (PStackX<R>)PersistentCollectionX.super.zip(other, zipper);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#permutations()
	 */
	@Override
	default PStackX<ReactiveSeq<T>> permutations() {
		
		return ( PStackX<ReactiveSeq<T>>)PersistentCollectionX.super.permutations();
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations(int)
	 */
	@Override
	default PStackX<ReactiveSeq<T>> combinations(int size) {
		
		return (PStackX<ReactiveSeq<T>>)PersistentCollectionX.super.combinations(size);
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#combinations()
	 */
	@Override
	default PStackX<ReactiveSeq<T>> combinations() {
		
		return (PStackX<ReactiveSeq<T>>)PersistentCollectionX.super.combinations();
	}

	default PStackX<ListX<T>> sliding(int windowSize){
		return  (PStackX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize);
	}
	default PStackX<ListX<T>> sliding(int windowSize, int increment){
		return  (PStackX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize,increment);
	}
	
	default <U> PStackX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return  (PStackX<U>)PersistentCollectionX.super.scanLeft(seed,function);
	}
	
	default <U> PStackX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return  (PStackX<U>)PersistentCollectionX.super.scanRight(identity,combiner);
	}
	default PStackX<T> scanLeft(Monoid<T> monoid){
		

		return  (PStackX<T>)PersistentCollectionX.super.scanLeft(monoid);
	
	}
	default PStackX<T> scanRight(Monoid<T> monoid){
		return  (PStackX<T>)PersistentCollectionX.super.scanRight(monoid);
	
	}
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(int)
	 */
	@Override
	default PStackX<T> cycle(int times) {
		
		return (PStackX<T>)PersistentCollectionX.super.cycle(times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default PStackX<T> cycle(Monoid<T> m, int times) {
		
		return (PStackX<T>)PersistentCollectionX.super.cycle(m, times);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return (PStackX<T>)PersistentCollectionX.super.cycleWhile(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return (PStackX<T>)PersistentCollectionX.super.cycleUntil(predicate);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> PStackX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return (PStackX<Tuple2<T, U>>)PersistentCollectionX.super.zipStream(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> PStackX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return (PStackX<Tuple2<T, U>>)PersistentCollectionX.super.zip(other);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> PStackX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return (PStackX)PersistentCollectionX.super.zip3(second, third);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> PStackX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return (PStackX<Tuple4<T, T2, T3, T4>>)PersistentCollectionX.super.zip4(second, third, fourth);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#zipWithIndex()
	 */
	@Override
	default PStackX<Tuple2<T, Long>> zipWithIndex() {
		
		return (PStackX<Tuple2<T, Long>>)PersistentCollectionX.super.zipWithIndex();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#distinct()
	 */
	@Override
	default PStackX<T> distinct() {
		
		return (PStackX<T>)PersistentCollectionX.super.distinct();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted()
	 */
	@Override
	default PStackX<T> sorted() {
		
		return (PStackX<T>)PersistentCollectionX.super.sorted();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default PStackX<T> sorted(Comparator<? super T> c) {
		
		return (PStackX<T>)PersistentCollectionX.super.sorted(c);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> skipWhile(Predicate<? super T> p) {
		
		return (PStackX<T>)PersistentCollectionX.super.skipWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> skipUntil(Predicate<? super T> p) {
		
		return (PStackX<T>)PersistentCollectionX.super.skipUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> limitWhile(Predicate<? super T> p) {
		
		return (PStackX<T>)PersistentCollectionX.super.limitWhile(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> limitUntil(Predicate<? super T> p) {
		
		return (PStackX<T>)PersistentCollectionX.super.limitUntil(p);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default PStackX<T> intersperse(T value) {
		
		return (PStackX<T>)PersistentCollectionX.super.intersperse(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle()
	 */
	@Override
	default PStackX<T> shuffle() {
		
		return (PStackX<T>)PersistentCollectionX.super.shuffle();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#skipLast(int)
	 */
	@Override
	default PStackX<T> skipLast(int num) {
		
		return (PStackX<T>)PersistentCollectionX.super.skipLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#limitLast(int)
	 */
	@Override
	default PStackX<T> limitLast(int num) {
		
		return (PStackX<T>)PersistentCollectionX.super.limitLast(num);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default PStackX<T> onEmpty(T value) {
		
		return (PStackX<T>)PersistentCollectionX.super.onEmpty(value);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default PStackX<T> onEmptyGet(Supplier<T> supplier) {
		
		return (PStackX<T>)PersistentCollectionX.super.onEmptyGet(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> PStackX<T> onEmptyThrow(Supplier<X> supplier) {
		
		return (PStackX<T>)PersistentCollectionX.super.onEmptyThrow(supplier);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#shuffle(java.util.Random)
	 */
	@Override
	default PStackX<T> shuffle(Random random) {
		
		return (PStackX<T>)PersistentCollectionX.super.shuffle(random);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#ofType(java.lang.Class)
	 */
	@Override
	default <U> PStackX<U> ofType(Class<U> type) {
		
		return (PStackX<U>)PersistentCollectionX.super.ofType(type);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> filterNot(Predicate<? super T> fn) {
		
		return (PStackX<T>)PersistentCollectionX.super.filterNot(fn);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#notNull()
	 */
	@Override
	default PStackX<T> notNull() {
		
		return (PStackX<T>)PersistentCollectionX.super.notNull();
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default PStackX<T> removeAll(Stream<T> stream) {
		
		return (PStackX<T>)PersistentCollectionX.super.removeAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default PStackX<T> removeAll(Iterable<T> it) {
		
		return (PStackX<T>)PersistentCollectionX.super.removeAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default PStackX<T> removeAll(T... values) {
		
		return (PStackX<T>)PersistentCollectionX.super.removeAll(values);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default PStackX<T> retainAll(Iterable<T> it) {
		
		return (PStackX<T>)PersistentCollectionX.super.retainAll(it);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default PStackX<T> retainAll(Stream<T> stream) {
		
		return (PStackX<T>)PersistentCollectionX.super.retainAll(stream);
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default PStackX<T> retainAll(T... values) {
		
		return (PStackX<T>)PersistentCollectionX.super.retainAll(values);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#cast(java.lang.Class)
	 */
	@Override
	default <U> PStackX<U> cast(Class<U> type) {
		
		return (PStackX<U>)PersistentCollectionX.super.cast(type);
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> PStackX<R> patternMatch(
			Function<CheckValues<T, R>, CheckValues<T, R>> case1,Supplier<? extends R> otherwise) {
		
		return (PStackX<R>)PersistentCollectionX.super.patternMatch(case1,otherwise);
	}
	 @Override
	    default <C extends Collection<? super T>> PStackX<C> grouped(int size, Supplier<C> supplier) {
	        
	        return (PStackX<C>)PersistentCollectionX.super.grouped(size, supplier);
	    }


	    @Override
	    default PStackX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
	        
	        return (PStackX<ListX<T>>)PersistentCollectionX.super.groupedUntil(predicate);
	    }


	    @Override
	    default PStackX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
	        
	        return (PStackX<ListX<T>>)PersistentCollectionX.super.groupedStatefullyWhile(predicate);
	    }


	    @Override
	    default PStackX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
	        
	        return (PStackX<ListX<T>>)PersistentCollectionX.super.groupedWhile(predicate);
	    }


	    @Override
	    default <C extends Collection<? super T>> PStackX<C> groupedWhile(Predicate<? super T> predicate,
	            Supplier<C> factory) {
	        
	        return (PStackX<C>)PersistentCollectionX.super.groupedWhile(predicate, factory);
	    }


	    @Override
	    default <C extends Collection<? super T>> PStackX<C> groupedUntil(Predicate<? super T> predicate,
	            Supplier<C> factory) {
	        
	        return (PStackX<C>)PersistentCollectionX.super.groupedUntil(predicate, factory);
	    }
	    @Override
	    default PStackX<T> removeAll(Seq<T> stream) {
	       
	        return (PStackX<T>)PersistentCollectionX.super.removeAll(stream);
	    }


	    @Override
	    default PStackX<T> retainAll(Seq<T> stream) {
	       
	        return (PStackX<T>)PersistentCollectionX.super.retainAll(stream);
	    }
	    
}
