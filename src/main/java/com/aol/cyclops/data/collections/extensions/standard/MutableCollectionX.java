package com.aol.cyclops.data.collections.extensions.standard;

import java.util.Collection;
import java.util.Comparator;
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

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable.CheckValue1;
import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.data.collections.extensions.CollectionX;
import com.aol.cyclops.data.collections.extensions.FluentCollectionX;
import com.aol.cyclops.util.stream.StreamUtils;

public interface MutableCollectionX<T> extends FluentCollectionX<T> {
	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Foldable#reduce(java.lang.Object, java.util.function.BiFunction)
	 */
	@Override
    default <U> U reduce(U identity, BiFunction<U, ? super T,U> accumulator){
	    return stream().reduce(identity,accumulator);
	}
	/**
	 * @param stream Create a MultableCollectionX from a Stream
	 * @return MutableCollectionX
	 */
	<X> MutableCollectionX<X> fromStream(Stream<X> stream);
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#combine(java.util.function.BiPredicate, java.util.function.BinaryOperator)
	 */
	@Override
	default MutableCollectionX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op){
	    return fromStream(stream().combine(predicate, op)); 
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#reverse()
	 */
	@Override
	default MutableCollectionX<T> reverse(){
		return fromStream(stream().reverse()); 
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#filter(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> filter(Predicate<? super T> pred){
		return fromStream(stream().filter(pred));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#map(java.util.function.Function)
	 */
	@Override
	default <R> CollectionX<R> map(Function<? super T, ? extends R> mapper){
		return fromStream(stream().map(mapper));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#flatMap(java.util.function.Function)
	 */
	@Override
	default <R> CollectionX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper){
		return fromStream(stream().flatMap(mapper.andThen(StreamUtils::stream)));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#limit(long)
	 */
	@Override
	default MutableCollectionX<T> limit(long num){
		return fromStream(stream().limit(num));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#skip(long)
	 */
	@Override
	default MutableCollectionX<T> skip(long num){
		return fromStream(stream().skip(num));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#takeRight(int)
	 */
	@Override
	default MutableCollectionX<T> takeRight(int num){
		return fromStream(stream().limitLast(num));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#dropRight(int)
	 */
	@Override
	default MutableCollectionX<T> dropRight(int num){
		return fromStream(stream().skipLast(num));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#takeWhile(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> takeWhile(Predicate<? super T> p){
		return fromStream(stream().limitWhile(p));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#dropWhile(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> dropWhile(Predicate<? super T> p){
		return fromStream(stream().skipWhile(p));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#takeUntil(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> takeUntil(Predicate<? super T> p){
		return fromStream(stream().limitUntil(p));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#dropUntil(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> dropUntil(Predicate<? super T> p){
		return fromStream(stream().skipUntil(p));
	}
	 
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#trampoline(java.util.function.Function)
	 */
	default <R> MutableCollectionX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		
		 return  fromStream(stream().trampoline(mapper));	 
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#slice(long, long)
	 */
	default MutableCollectionX<T> slice(long from, long to){
		return fromStream(stream().slice(from,to));	 
	}
	
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#grouped(int)
	 */
	default MutableCollectionX<ListX<T>> grouped(int groupSize){
		return fromStream(stream().grouped(groupSize).map(ListX::fromIterable));	 
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#grouped(java.util.function.Function, java.util.stream.Collector)
	 */
	default <K, A, D> MutableCollectionX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return fromStream(stream().grouped(classifier,downstream));	 
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#grouped(java.util.function.Function)
	 */
	default <K> MutableCollectionX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return fromStream(stream().grouped(classifier));	 
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip(java.lang.Iterable)
	 */
	default <U> MutableCollectionX<Tuple2<T, U>> zip(Iterable<? extends U> other){
		return fromStream(stream().zip(other));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip(java.lang.Iterable, java.util.function.BiFunction)
	 */
	@Override
	default <U, R> MutableCollectionX<R> zip(Iterable<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper){
		return fromStream(stream().zip(other,zipper));
	}
	@Override
	default <U, R> MutableCollectionX<R> zip(Seq<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper){
        return fromStream(stream().zip(other,zipper));
    }
	@Override
    default <U, R> MutableCollectionX<R> zip(Stream<? extends U> other, BiFunction<? super T, ? super U, ? extends R> zipper){
        return fromStream(stream().zip(other,zipper));
    }
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#sliding(int)
	 */
	default MutableCollectionX<ListX<T>> sliding(int windowSize){
		return fromStream(stream().sliding(windowSize).map(ListX::fromIterable));	
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#sliding(int, int)
	 */
	default MutableCollectionX<ListX<T>> sliding(int windowSize, int increment){
		return fromStream(stream().sliding(windowSize,increment).map(ListX::fromIterable));	
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#scanLeft(com.aol.cyclops.Monoid)
	 */
	default MutableCollectionX<T> scanLeft(Monoid<T> monoid){
		return fromStream(stream().scanLeft(monoid));	
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#scanLeft(java.lang.Object, java.util.function.BiFunction)
	 */
	default <U> MutableCollectionX<U> scanLeft(U seed, BiFunction<? super U, ? super T, ? extends U> function){
		return fromStream(stream().scanLeft(seed,function));	
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#scanRight(com.aol.cyclops.Monoid)
	 */
	default MutableCollectionX<T> scanRight(Monoid<T> monoid){
		return fromStream(stream().scanRight(monoid));	
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#scanRight(java.lang.Object, java.util.function.BiFunction)
	 */
	default <U> MutableCollectionX<U> scanRight(U identity, BiFunction<? super T, ? super U,? extends U> combiner){
		return fromStream(stream().scanRight(identity,combiner));
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#sorted(java.util.function.Function)
	 */
	default <U extends Comparable<? super U>> MutableCollectionX<T> sorted(Function<? super T, ? extends U> function){
		return fromStream(stream().sorted(function));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#plus(java.lang.Object)
	 */
	default MutableCollectionX<T> plus(T e){
		add(e);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#plusAll(java.util.Collection)
	 */
	default MutableCollectionX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#minus(java.lang.Object)
	 */
	default MutableCollectionX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.FluentCollectionX#minusAll(java.util.Collection)
	 */
	default MutableCollectionX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#cycle(int)
	 */
	@Override
	default MutableCollectionX<T> cycle(int times) {
		
		return fromStream(stream().cycle(times));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#cycle(com.aol.cyclops.Monoid, int)
	 */
	@Override
	default MutableCollectionX<T> cycle(Monoid<T> m, int times) {
		
		return fromStream(stream().cycle(m, times));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return fromStream(stream().cycleWhile(predicate));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return fromStream(stream().cycleUntil(predicate));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip(java.util.stream.Stream)
	 */
	@Override
	default <U> MutableCollectionX<Tuple2<T, U>> zip(Stream<? extends U> other) {
		
		return fromStream(stream().zip(other));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> MutableCollectionX<Tuple2<T, U>> zip(Seq<? extends U> other) {
		
		return fromStream(stream().zip(other));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> MutableCollectionX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return fromStream(stream().zip3(second, third));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> MutableCollectionX<Tuple4<T, T2, T3, T4>> zip4(Stream<? extends T2> second, Stream<? extends T3> third,
			Stream<? extends T4> fourth) {
		
		return fromStream(stream().zip4(second, third, fourth));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#zipWithIndex()
	 */
	@Override
	default MutableCollectionX<Tuple2<T, Long>> zipWithIndex() {
		
		return fromStream(stream().zipWithIndex());
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#distinct()
	 */
	@Override
	default MutableCollectionX<T> distinct() {
		
		return fromStream(stream().distinct());
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#sorted()
	 */
	@Override
	default MutableCollectionX<T> sorted() {
		
		return fromStream(stream().sorted());
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#sorted(java.util.Comparator)
	 */
	@Override
	default MutableCollectionX<T> sorted(Comparator<? super T> c) {
		
		return fromStream(stream().sorted(c));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> skipWhile(Predicate<? super T> p) {
		
		return fromStream(stream().skipWhile(p));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> skipUntil(Predicate<? super T> p) {
		
		return fromStream(stream().skipUntil(p));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> limitWhile(Predicate<? super T> p) {
		
		return fromStream(stream().limitWhile(p));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> limitUntil(Predicate<? super T> p) {
		
		return fromStream(stream().limitUntil(p));
	}

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#intersperse(java.lang.Object)
	 */
	@Override
	default MutableCollectionX<T> intersperse(T value) {
		
		return fromStream(stream().intersperse(value));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#shuffle()
	 */
	@Override
	default MutableCollectionX<T> shuffle() {
		
		return fromStream(stream().shuffle());
	}

	


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#skipLast(int)
	 */
	@Override
	default MutableCollectionX<T> skipLast(int num) {
		
		return fromStream(stream().skipLast(num));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#limitLast(int)
	 */
	@Override
	default MutableCollectionX<T> limitLast(int num) {
	
		return fromStream(stream().limitLast(num));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#onEmpty(java.lang.Object)
	 */
	@Override
	default MutableCollectionX<T> onEmpty(T value) {
		return fromStream(stream().onEmpty(value));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default MutableCollectionX<T> onEmptyGet(Supplier<? extends T> supplier) {
		return fromStream(stream().onEmptyGet(supplier));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> MutableCollectionX<T> onEmptyThrow(Supplier<? extends X> supplier) {
		return fromStream(stream().onEmptyThrow(supplier));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#shuffle(java.util.Random)
	 */
	@Override
	default MutableCollectionX<T> shuffle(Random random) {
		return fromStream(stream().shuffle(random));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.types.Filterable#ofType(java.lang.Class)
	 */
	@Override
	default <U> MutableCollectionX<U> ofType(Class<? extends U> type) {
		
		return (MutableCollectionX)FluentCollectionX.super.ofType(type);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#filterNot(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> filterNot(Predicate<? super T> fn) {
	    return fromStream(stream().filterNot(fn));
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#notNull()
	 */
	@Override
	default MutableCollectionX<T> notNull() {
	    return fromStream(stream().notNull());
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.util.stream.Stream)
	 */
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#removeAll(java.util.stream.Stream)
	 */
	@Override
	default MutableCollectionX<T> removeAll(Stream<? extends T> stream) {
		
	    return fromStream(stream().removeAll(stream));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#removeAll(org.jooq.lambda.Seq)
	 */
	@Override
    default MutableCollectionX<T> removeAll(Seq<? extends T> stream) {
        
        return fromStream(stream().removeAll(stream));
    }


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#removeAll(java.lang.Iterable)
	 */
	@Override
	default MutableCollectionX<T> removeAll(Iterable<? extends T> it) {
	    return fromStream(stream().removeAll(it));
		
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#removeAll(java.lang.Object[])
	 */
	@Override
	default MutableCollectionX<T> removeAll(T... values) {
	    return fromStream(stream().removeAll(values));
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#retainAll(java.lang.Iterable)
	 */
	@Override
	default MutableCollectionX<T> retainAll(Iterable<? extends T> it) {
	    return fromStream(stream().retainAll(it));
	}


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#retainAll(java.util.stream.Stream)
	 */
	@Override
	default MutableCollectionX<T> retainAll(Stream<? extends T> stream) {
	    return fromStream(stream().retainAll(stream));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#retainAll(org.jooq.lambda.Seq)
	 */
	@Override
    default MutableCollectionX<T> retainAll(Seq<? extends T> stream) {
        return fromStream(stream().retainAll(stream));
    }

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#retainAll(java.lang.Object[])
	 */
	@Override
	default MutableCollectionX<T> retainAll(T... values) {
	    return fromStream(stream().retainAll(values));
	}

	


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#cast(java.lang.Class)
	 */
	@Override
	default <U> MutableCollectionX<U> cast(Class<? extends U> type) {
	    return fromStream(stream().cast(type));
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#patternMatch(java.util.function.Function, java.util.function.Supplier)
	 */
	@Override
	default <R> MutableCollectionX<R> patternMatch(
			Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise) {
		
	    return fromStream(stream().patternMatch(case1, otherwise));
	}

	


	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#permutations()
	 */
	@Override
	default MutableCollectionX<ReactiveSeq<T>> permutations() {
		return fromStream(stream().permutations());
		
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations(int)
	 */
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#combinations(int)
	 */
	@Override
	default MutableCollectionX<ReactiveSeq<T>> combinations(int size) {
		return fromStream(stream().combinations(size));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations()
	 */
	/* (non-Javadoc)
	 * @see com.aol.cyclops.data.collections.extensions.CollectionX#combinations()
	 */
	@Override
	default MutableCollectionX<ReactiveSeq<T>> combinations() {
		return fromStream(stream().combinations());
	}

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#grouped(int, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> grouped(int size, Supplier<C> supplier) {
        
        return fromStream(stream().grouped(size,supplier));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedUntil(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        
        return fromStream(stream().groupedUntil(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedWhile(java.util.function.Predicate)
     */
    @Override
    default MutableCollectionX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        
        return fromStream(stream().groupedWhile(predicate));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedWhile(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return fromStream(stream().groupedWhile(predicate,factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedUntil(java.util.function.Predicate, java.util.function.Supplier)
     */
    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return fromStream(stream().groupedUntil(predicate,factory));
    }

    /* (non-Javadoc)
     * @see com.aol.cyclops.data.collections.extensions.CollectionX#groupedStatefullyWhile(java.util.function.BiPredicate)
     */
    @Override
    default MutableCollectionX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return fromStream(stream().groupedStatefullyWhile(predicate));
    }
    
	
	
}
