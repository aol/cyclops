package com.aol.cyclops.data.collections.extensions.persistent;

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

import org.hamcrest.Matcher;
import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;
import org.jooq.lambda.tuple.Tuple3;
import org.jooq.lambda.tuple.Tuple4;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.Reducer;
import com.aol.cyclops.control.Matchable.CheckValues;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.data.collections.extensions.standard.ListX;
import com.aol.cyclops.data.collections.extensions.standard.MutableCollectionX;
import com.aol.cyclops.types.Traversable;
import com.aol.cyclops.util.stream.StreamUtils;

import lombok.AllArgsConstructor;

public interface PersistentCollectionX<T> extends FluentCollectionX<T>{
    @AllArgsConstructor
    static class LazyCollection<T,C extends Collection<T>>{
        private volatile C list;
        private volatile Stream<T> seq;
        private final Reducer<C> reducer;
        public C get(){
            if( seq!=null){
               list =  reducer.mapReduce(seq);
               seq = null;
            }
              
            return list;
            
        }
        public ReactiveSeq<T> stream(){
            if(seq!=null)
                return ReactiveSeq.fromStream(seq);
            return ReactiveSeq.fromIterable(list);
        }
    }
    <X> PersistentCollectionX<X> stream(Stream<X> stream);
    @Override
	ReactiveSeq<T> stream();
	default PersistentCollectionX<T> plusInOrder(T e){
		return (PersistentCollectionX<T>)FluentCollectionX.super.plusInOrder(e);
	}
	public <R> PersistentCollectionX<R> unit(Collection<R> col);
	<R> PersistentCollectionX<R> emptyUnit();
	<T> Reducer<? extends Collection<T>> monoid();
	
	<T1> PersistentCollectionX<T1> from(Collection<T1> c);
	default CollectionX<T> reverse(){
		return stream(stream().reverse()); 
	}
	default PersistentCollectionX<T> filter(Predicate<? super T> pred){
		return stream(stream().filter(pred));
	
	}
	default <R> PersistentCollectionX<R> map(Function<? super T, ? extends R> mapper){
		return stream(stream().map(mapper));
	}
	default <R> PersistentCollectionX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper){
		return stream(stream().flatMap(mapper.andThen(StreamUtils::stream)));
	}
	default PersistentCollectionX<T> limit(long num){
		return stream(stream().limit(num));
		
	}
	default PersistentCollectionX<T> skip(long num){
		
		return stream(stream().skip(num));
	}
	default PersistentCollectionX<T> dropRight(int num){
		return stream(stream().skipLast(num));
	}
	default PersistentCollectionX<T> takeRight(int num){
		return stream(stream().limitLast(num));
	}
	default PersistentCollectionX<T> takeWhile(Predicate<? super T> p){
		return stream(stream().limitWhile(p));
	}
	default PersistentCollectionX<T> dropWhile(Predicate<? super T> p){
		return stream(stream().skipWhile(p));
	}
	default PersistentCollectionX<T> takeUntil(Predicate<? super T> p){
		return stream(stream().limitUntil(p));
	}
	default PersistentCollectionX<T> dropUntil(Predicate<? super T> p){
		return stream(stream().skipUntil(p));
	}
	 /**
	  * Performs a map operation that can call a recursive method without running out of stack space
	  * <pre>
	  * {@code
	  * ReactiveSeq.of(10,20,30,40)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println); 
				 
		Trampoline<Long> fibonacci(int i){
			return fibonacci(i,1,0);
		}
		Trampoline<Long> fibonacci(int n, long a, long b) {
	    	return n == 0 ? Trampoline.done(b) : Trampoline.more( ()->fibonacci(n-1, a+b, a));
		}		 
				 
	  * 55
		6765
		832040
		102334155
	  * 
	  * 
	  * ReactiveSeq.of(10_000,200_000,3_000_000,40_000_000)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println);
				 
				 
	  * completes successfully
	  * }
	  * 
	 * @param mapper
	 * @return
	 */
	default <R> PersistentCollectionX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		return map(in-> mapper.apply(in).result()); 
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	default PersistentCollectionX<T> slice(long from, long to){
		return stream(stream().slice(from,to));	 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	default <U extends Comparable<? super U>> PersistentCollectionX<T> sorted(Function<? super T, ? extends U> function){
		return stream((stream().sorted(function)));
	}
	
	
	
	default PersistentCollectionX<ListX<T>> grouped(int groupSize){
		return stream(stream().grouped(groupSize).map(ListX::fromIterable));
	}
	default <K, A, D> PersistentCollectionX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return stream(stream().grouped(classifier,downstream));
	}
	default <K> PersistentCollectionX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return stream(stream().grouped(classifier));
	}
	default <U> PersistentCollectionX<Tuple2<T, U>> zip(Iterable<U> other){
		return stream(stream().zip(other));
	}
	default <U, R> PersistentCollectionX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper){
		return stream(stream().zip(other,zipper));
	}
	default PersistentCollectionX<ListX<T>> sliding(int windowSize){
		return stream(stream().sliding(windowSize));
	}
	default PersistentCollectionX<ListX<T>> sliding(int windowSize, int increment){
		return stream(stream().sliding(windowSize,increment));
	}
	default PersistentCollectionX<T> scanLeft(Monoid<T> monoid){
		return stream(stream().scanLeft(monoid));
	}
	default PersistentCollectionX<T> scanRight(Monoid<T> monoid){
		return stream(stream().scanRight(monoid));
	}
	default <U> PersistentCollectionX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return stream(stream().scanLeft(seed,function));
	}
	
	default <U> PersistentCollectionX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return stream(stream().scanRight(identity,combiner));
	}
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycle(int)
	 */
	@Override
	default PersistentCollectionX<T> cycle(int times) {
		
		return stream(stream().cycle(times));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default PersistentCollectionX<T> cycle(Monoid<T> m, int times) {
		
		return stream(stream().cycle(m, times));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default PersistentCollectionX<T> cycleWhile(Predicate<? super T> predicate) {
		return stream(stream().cycleWhile(predicate));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default PersistentCollectionX<T> cycleUntil(Predicate<? super T> predicate) {
		return stream(stream().cycleUntil(predicate));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> PersistentCollectionX<Tuple2<T, U>> zipStream(Stream<U> other) {
		return stream(stream().zipStream(other));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> PersistentCollectionX<Tuple2<T, U>> zip(Seq<U> other) {
		return stream(stream().zip(other));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> PersistentCollectionX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		return stream(stream().zip3(second, third));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> PersistentCollectionX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		return stream(stream().zip4(second, third, fourth));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zipWithIndex()
	 */
	@Override
	default PersistentCollectionX<Tuple2<T, Long>> zipWithIndex() {
		return stream(stream().zipWithIndex());
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#distinct()
	 */
	@Override
	default PersistentCollectionX<T> distinct() {
		return stream(stream().distinct());
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#sorted()
	 */
	@Override
	default PersistentCollectionX<T> sorted() {
		return stream(stream().sorted());
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#sorted(java.util.Comparator)
	 */
	@Override
	default PersistentCollectionX<T> sorted(Comparator<? super T> c) {
		return stream(stream().sorted(c));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default PersistentCollectionX<T> skipWhile(Predicate<? super T> p) {
		return stream(stream().skipWhile(p));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default PersistentCollectionX<T> skipUntil(Predicate<? super T> p) {
		return stream(stream().skipUntil(p));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default PersistentCollectionX<T> limitWhile(Predicate<? super T> p) {
		return stream(stream().limitWhile(p));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default PersistentCollectionX<T> limitUntil(Predicate<? super T> p) {
		return stream(stream().limitUntil(p));
	}
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#intersperse(java.lang.Object)
	 */
	@Override
	default PersistentCollectionX<T> intersperse(T value) {
		return stream(stream().intersperse(value));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#shuffle()
	 */
	@Override
	default PersistentCollectionX<T> shuffle() {
		return stream(stream().shuffle());
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipLast(int)
	 */
	@Override
	default PersistentCollectionX<T> skipLast(int num) {
		return stream(stream().skipLast(num));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#limitLast(int)
	 */
	@Override
	default PersistentCollectionX<T> limitLast(int num) {
		return stream(stream().limitLast(num));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#onEmpty(java.lang.Object)
	 */
	@Override
	default PersistentCollectionX<T> onEmpty(T value) {
		return stream(stream().onEmpty(value));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default PersistentCollectionX<T> onEmptyGet(Supplier<T> supplier) {
		return stream(stream().onEmptyGet(supplier));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> PersistentCollectionX<T> onEmptyThrow(Supplier<X> supplier) {
		return stream(stream().onEmptyThrow(supplier));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#shuffle(java.util.Random)
	 */
	@Override
	default PersistentCollectionX<T> shuffle(Random random) {
		return stream(stream().shuffle(random));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#ofType(java.lang.Class)
	 */
	@Override
	default <U> PersistentCollectionX<U> ofType(Class<U> type) {
		return stream(stream().ofType(type));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
	 */
	@Override
	default PersistentCollectionX<T> filterNot(Predicate<? super T> fn) {
		 return stream(stream().filterNot(fn));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	@Override
	default PersistentCollectionX<T> notNull() {
		return stream(stream().notNull());
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.util.stream.Stream)
	 */
	@Override
	default PersistentCollectionX<T> removeAll(Stream<T> stream) {
		return stream(stream().removeAll(stream));
	}
	@Override
    default PersistentCollectionX<T> removeAll(Seq<T> stream) {
        return stream(stream().removeAll(stream));
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Iterable)
	 */
	@Override
	default PersistentCollectionX<T> removeAll(Iterable<T> it) {
		return stream(stream().removeAll(it));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Object[])
	 */
	@Override
	default PersistentCollectionX<T> removeAll(T... values) {
		return stream(stream().removeAll(values));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Iterable)
	 */
	@Override
	default PersistentCollectionX<T> retainAll(Iterable<T> it) {
		 return stream(stream().retainAll(it));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.util.stream.Stream)
	 */
	@Override
	default PersistentCollectionX<T> retainAll(Stream<T> stream) {
		 return stream(stream().retainAll(stream));
	}
	@Override
    default PersistentCollectionX<T> retainAll(Seq<T> stream) {
        return stream(stream().retainAll(stream));
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Object[])
	 */
	@Override
	default PersistentCollectionX<T> retainAll(T... values) {
		 return stream(stream().retainAll(values));
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> PersistentCollectionX<U> cast(Class<U> type) {
		return stream(stream().cast(type));
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> PersistentCollectionX<R> patternMatch(
			Function<CheckValues<T, R>, CheckValues<T, R>> case1,Supplier<? extends R> otherwise) {
		return stream(stream().patternMatch(case1,otherwise));
	}
	
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#permutations()
	 */
	@Override
	default PersistentCollectionX<ReactiveSeq<T>> permutations() {
		return stream(stream().permutations());
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations(int)
	 */
	@Override
	default PersistentCollectionX<ReactiveSeq<T>> combinations(int size) {
		return stream(stream().combinations(size));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations()
	 */
	@Override
	default PersistentCollectionX<ReactiveSeq<T>> combinations() {
		return stream(stream().combinations());
	}
    @Override
    default <C extends Collection<? super T>> PersistentCollectionX<C> grouped(int size, Supplier<C> supplier) {
        
        return stream(stream().grouped(size, supplier));
    }
    @Override
    default PersistentCollectionX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        return stream(stream().groupedUntil(predicate));
    }
    @Override
    default PersistentCollectionX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return stream(stream().groupedStatefullyWhile(predicate));
    }
    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
     */
    @Override
    default PersistentCollectionX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op) {
        return stream(stream().combine(predicate,op));
    }
    @Override
    default PersistentCollectionX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        return stream(stream().groupedWhile(predicate));
    }
    @Override
    default <C extends Collection<? super T>> PersistentCollectionX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        return stream(stream().groupedWhile(predicate,factory));
    }
    @Override
    default <C extends Collection<? super T>> PersistentCollectionX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        return stream(stream().groupedUntil(predicate,factory));
    }

    
	
	
	
	
}
