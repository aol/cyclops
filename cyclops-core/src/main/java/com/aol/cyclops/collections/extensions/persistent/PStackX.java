package com.aol.cyclops.collections.extensions.persistent;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.pcollections.ConsPStack;
import org.pcollections.PStack;

import com.aol.cyclops.collections.PStacks;
import com.aol.cyclops.collections.extensions.FluentSequenceX;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trampoline.Trampoline;

public interface PStackX<T> extends PStack<T>, PersistentCollectionX<T>, FluentSequenceX<T>{
	
	//after module merge, move to reducers
	public static <T> Monoid<PStackX<T>> toPStackX() { 
		return	Monoid.<PStackX<T>>of(PStackX.empty(), 
								(PStackX<T> a) -> b -> a.plusAll(b),
								(T x) -> PStackX.singleton(x));
	}
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
		return new PStackXImpl<>(ConsPStack.from(Arrays.asList(values)));
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
		if(values instanceof PStack)
			return new PStackXImpl<>((PStack)values);
		return new PStackXImpl<>(ConsPStack.from(values));
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
		return new PStackXImpl<>(ConsPStack.empty());
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
		return new PStackXImpl<>(ConsPStack.singleton(value));
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
		return new PStackXImpl<>((PStack<T>)PStacks.toPStack().mapReduce(stream));
	}
	default PStack<T> toPStack(){
		return this;
	}
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	default <X> PStackX<X> from(Collection<X> col){
		return fromCollection(col);
	}
	default <T> Monoid<PStack<T>> monoid(){
		return PStacks.toPStack();
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

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default PStackX<T> filter(Predicate<? super T> pred) {
		
		return (PStackX)PersistentCollectionX.super.filter(pred);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.collections.extensions.persistent.PersistentCollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> PStackX<R> map(Function<? super T, ? extends R> mapper) {
		
		return (PStackX)PersistentCollectionX.super.map(mapper);
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
	default PStackX<ListX<T>> sliding(int windowSize){
		return  (PStackX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize);
	}
	default PStackX<ListX<T>> sliding(int windowSize, int increment){
		return  (PStackX<ListX<T>>)PersistentCollectionX.super.sliding(windowSize,increment);
	}
	default PStackX<T> scanLeft(Monoid<T> monoid){
		return  (PStackX<T>)PersistentCollectionX.super.scanLeft(monoid);
	}
	default <U> PStackX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return  (PStackX<U>)PersistentCollectionX.super.scanLeft(seed,function);
	}
	default PStackX<T> scanRight(Monoid<T> monoid){
		return  (PStackX<T>)PersistentCollectionX.super.scanRight(monoid);
	}
	default <U> PStackX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return  (PStackX<U>)PersistentCollectionX.super.scanRight(identity,combiner);
	}
}
