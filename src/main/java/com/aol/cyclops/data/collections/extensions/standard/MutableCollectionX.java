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
import com.aol.cyclops.data.collections.extensions.LazyFluentCollection;
import com.aol.cyclops.util.stream.StreamUtils;

import lombok.AllArgsConstructor;

public interface MutableCollectionX<T> extends FluentCollectionX<T> {
    @AllArgsConstructor
    static class LazyCollection<T,C extends Collection<T>> implements LazyFluentCollection<T,C>{
        private volatile C list;
        private volatile Stream<T> seq;
        private final Collector<T,?,C> collector;
        public C get(){
            if( seq!=null){
               list =  seq.collect(collector);
               seq = null;
            }
              
            return list;
            
        }
        
        public ReactiveSeq<T> stream(){
            if(seq!=null){
               ReactiveSeq<T> result =  ReactiveSeq.fromStream(seq);
               seq=null;
               return result;
            }
            return ReactiveSeq.fromIterable(list);
        }
    }
  //  public CollectionX<T> immutable();
	<X> MutableCollectionX<X> stream(Stream<X> stream);
	@Override
	default MutableCollectionX<T> combine(BiPredicate<? super T, ? super T> predicate, BinaryOperator<T> op){
	    return stream(stream().combine(predicate, op)); 
	}
	@Override
	default MutableCollectionX<T> reverse(){
		return stream(stream().reverse()); 
	}
	@Override
	default MutableCollectionX<T> filter(Predicate<? super T> pred){
		return stream(stream().filter(pred));
	}
	@Override
	default <R> CollectionX<R> map(Function<? super T, ? extends R> mapper){
		return stream(stream().map(mapper));
	}
	@Override
	default <R> CollectionX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper){
		return stream(stream().flatMap(mapper.andThen(StreamUtils::stream)));
	}
	@Override
	default MutableCollectionX<T> limit(long num){
		return stream(stream().limit(num));
	}
	@Override
	default MutableCollectionX<T> skip(long num){
		return stream(stream().skip(num));
	}
	@Override
	default MutableCollectionX<T> takeRight(int num){
		return stream(stream().limitLast(num));
	}
	@Override
	default MutableCollectionX<T> dropRight(int num){
		return stream(stream().skipLast(num));
	}
	@Override
	default MutableCollectionX<T> takeWhile(Predicate<? super T> p){
		return stream(stream().limitWhile(p));
	}
	@Override
	default MutableCollectionX<T> dropWhile(Predicate<? super T> p){
		return stream(stream().skipWhile(p));
	}
	@Override
	default MutableCollectionX<T> takeUntil(Predicate<? super T> p){
		return stream(stream().limitUntil(p));
	}
	@Override
	default MutableCollectionX<T> dropUntil(Predicate<? super T> p){
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
	default <R> MutableCollectionX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		
		 return  stream(stream().trampoline(mapper));	 
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	default MutableCollectionX<T> slice(long from, long to){
		return stream(stream().slice(from,to));	 
	}
	
	

	default MutableCollectionX<ListX<T>> grouped(int groupSize){
		return stream(stream().grouped(groupSize).map(ListX::fromIterable));	 
	}
	default <K, A, D> MutableCollectionX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return stream(stream().grouped(classifier,downstream));	 
	}
	default <K> MutableCollectionX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return stream(stream().grouped(classifier));	 
	}
	default <U> MutableCollectionX<Tuple2<T, U>> zip(Iterable<U> other){
		return stream(stream().zip(other));
	}
	default <U, R> MutableCollectionX<R> zip(Iterable<U> other, BiFunction<? super T, ? super U, ? extends R> zipper){
		return stream(stream().zip(other,zipper));
	}
	default MutableCollectionX<ListX<T>> sliding(int windowSize){
		return stream(stream().sliding(windowSize).map(ListX::fromIterable));	
	}
	default MutableCollectionX<ListX<T>> sliding(int windowSize, int increment){
		return stream(stream().sliding(windowSize,increment).map(ListX::fromIterable));	
	}
	default MutableCollectionX<T> scanLeft(Monoid<T> monoid){
		return stream(stream().scanLeft(monoid));	
	}
	default <U> MutableCollectionX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return stream(stream().scanLeft(seed,function));	
	}
	default MutableCollectionX<T> scanRight(Monoid<T> monoid){
		return stream(stream().scanRight(monoid));	
	}
	default <U> MutableCollectionX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return stream(stream().scanRight(identity,combiner));
	}
	

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	default <U extends Comparable<? super U>> MutableCollectionX<T> sorted(Function<? super T, ? extends U> function){
		return stream(stream().sorted(function));
	}
	default MutableCollectionX<T> plus(T e){
		add(e);
		return this;
	}
	
	default MutableCollectionX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	
	default MutableCollectionX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	default MutableCollectionX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}
	default MutableCollectionX<T> plusLazy(T e){
        add(e);
        return this;
    }
    
    default MutableCollectionX<T> plusAllLazy(Collection<? extends T> list){
        addAll(list);
        return this;
    }
    
    default MutableCollectionX<T> minusLazy(Object e){
        remove(e);
        return this;
    }
    
    default MutableCollectionX<T> minusAllLazy(Collection<?> list){
        removeAll(list);
        return this;
    }



	


	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycle(int)
	 */
	@Override
	default MutableCollectionX<T> cycle(int times) {
		
		return stream(stream().cycle(times));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycle(com.aol.cyclops.sequence.Monoid, int)
	 */
	@Override
	default MutableCollectionX<T> cycle(Monoid<T> m, int times) {
		
		return stream(stream().cycle(m, times));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycleWhile(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> cycleWhile(Predicate<? super T> predicate) {
		
		return stream(stream().cycleWhile(predicate));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#cycleUntil(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> cycleUntil(Predicate<? super T> predicate) {
		
		return stream(stream().cycleUntil(predicate));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zipStream(java.util.stream.Stream)
	 */
	@Override
	default <U> MutableCollectionX<Tuple2<T, U>> zipStream(Stream<U> other) {
		
		return stream(stream().zipStream(other));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip(org.jooq.lambda.Seq)
	 */
	@Override
	default <U> MutableCollectionX<Tuple2<T, U>> zip(Seq<U> other) {
		
		return stream(stream().zip(other));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip3(java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <S, U> MutableCollectionX<Tuple3<T, S, U>> zip3(Stream<? extends S> second, Stream<? extends U> third) {
		
		return stream(stream().zip3(second, third));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zip4(java.util.stream.Stream, java.util.stream.Stream, java.util.stream.Stream)
	 */
	@Override
	default <T2, T3, T4> MutableCollectionX<Tuple4<T, T2, T3, T4>> zip4(Stream<T2> second, Stream<T3> third,
			Stream<T4> fourth) {
		
		return stream(stream().zip4(second, third, fourth));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#zipWithIndex()
	 */
	@Override
	default MutableCollectionX<Tuple2<T, Long>> zipWithIndex() {
		
		return stream(stream().zipWithIndex());
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#distinct()
	 */
	@Override
	default MutableCollectionX<T> distinct() {
		
		return stream(stream().distinct());
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#sorted()
	 */
	@Override
	default MutableCollectionX<T> sorted() {
		
		return stream(stream().sorted());
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#sorted(java.util.Comparator)
	 */
	@Override
	default MutableCollectionX<T> sorted(Comparator<? super T> c) {
		
		return stream(stream().sorted(c));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipWhile(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> skipWhile(Predicate<? super T> p) {
		
		return stream(stream().skipWhile(p));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipUntil(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> skipUntil(Predicate<? super T> p) {
		
		return stream(stream().skipUntil(p));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#limitWhile(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> limitWhile(Predicate<? super T> p) {
		
		return stream(stream().limitWhile(p));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#limitUntil(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> limitUntil(Predicate<? super T> p) {
		
		return stream(stream().limitUntil(p));
	}

	
	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#intersperse(java.lang.Object)
	 */
	@Override
	default MutableCollectionX<T> intersperse(T value) {
		
		return stream(stream().intersperse(value));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#shuffle()
	 */
	@Override
	default MutableCollectionX<T> shuffle() {
		
		return stream(stream().shuffle());
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#skipLast(int)
	 */
	@Override
	default MutableCollectionX<T> skipLast(int num) {
		
		return stream(stream().skipLast(num));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#limitLast(int)
	 */
	@Override
	default MutableCollectionX<T> limitLast(int num) {
	
		return stream(stream().limitLast(num));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#onEmpty(java.lang.Object)
	 */
	@Override
	default MutableCollectionX<T> onEmpty(T value) {
		return stream(stream().onEmpty(value));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#onEmptyGet(java.util.function.Supplier)
	 */
	@Override
	default MutableCollectionX<T> onEmptyGet(Supplier<T> supplier) {
		return stream(stream().onEmptyGet(supplier));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#onEmptyThrow(java.util.function.Supplier)
	 */
	@Override
	default <X extends Throwable> MutableCollectionX<T> onEmptyThrow(Supplier<X> supplier) {
		return stream(stream().onEmptyThrow(supplier));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Traversable#shuffle(java.util.Random)
	 */
	@Override
	default MutableCollectionX<T> shuffle(Random random) {
		return stream(stream().shuffle(random));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#ofType(java.lang.Class)
	 */
	@Override
	default <U> MutableCollectionX<U> ofType(Class<U> type) {
		
		return (MutableCollectionX)FluentCollectionX.super.ofType(type);
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#filterNot(java.util.function.Predicate)
	 */
	@Override
	default MutableCollectionX<T> filterNot(Predicate<? super T> fn) {
	    return stream(stream().filterNot(fn));
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#notNull()
	 */
	@Override
	default MutableCollectionX<T> notNull() {
	    return stream(stream().notNull());
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.util.stream.Stream)
	 */
	@Override
	default MutableCollectionX<T> removeAll(Stream<T> stream) {
		
	    return stream(stream().removeAll(stream));
	}
	@Override
    default MutableCollectionX<T> removeAll(Seq<T> stream) {
        
        return stream(stream().removeAll(stream));
    }

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Iterable)
	 */
	@Override
	default MutableCollectionX<T> removeAll(Iterable<T> it) {
	    return stream(stream().removeAll(it));
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#removeAll(java.lang.Object[])
	 */
	@Override
	default MutableCollectionX<T> removeAll(T... values) {
	    return stream(stream().removeAll(values));
		
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Iterable)
	 */
	@Override
	default MutableCollectionX<T> retainAll(Iterable<T> it) {
	    return stream(stream().retainAll(it));
	}

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.util.stream.Stream)
	 */
	@Override
	default MutableCollectionX<T> retainAll(Stream<T> stream) {
	    return stream(stream().retainAll(stream));
	}
	@Override
    default MutableCollectionX<T> retainAll(Seq<T> stream) {
        return stream(stream().retainAll(stream));
    }

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Filterable#retainAll(java.lang.Object[])
	 */
	@Override
	default MutableCollectionX<T> retainAll(T... values) {
	    return stream(stream().retainAll(values));
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#cast(java.lang.Class)
	 */
	@Override
	default <U> MutableCollectionX<U> cast(Class<U> type) {
	    return stream(stream().cast(type));
	}

	
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#patternMatch(java.lang.Object, java.util.function.Function)
	 */
	@Override
	default <R> MutableCollectionX<R> patternMatch(
			Function<CheckValue1<T, R>, CheckValue1<T, R>> case1,Supplier<? extends R> otherwise) {
		
	    return stream(stream().patternMatch(case1, otherwise));
	}

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#permutations()
	 */
	@Override
	default MutableCollectionX<ReactiveSeq<T>> permutations() {
		return stream(stream().permutations());
		
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations(int)
	 */
	@Override
	default MutableCollectionX<ReactiveSeq<T>> combinations(int size) {
		return stream(stream().combinations(size));
	}
	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.ExtendedTraversable#combinations()
	 */
	@Override
	default MutableCollectionX<ReactiveSeq<T>> combinations() {
		return stream(stream().combinations());
	}

    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> grouped(int size, Supplier<C> supplier) {
        
        return stream(stream().grouped(size,supplier));
    }

    @Override
    default MutableCollectionX<ListX<T>> groupedUntil(Predicate<? super T> predicate) {
        
        return stream(stream().groupedUntil(predicate));
    }

    @Override
    default MutableCollectionX<ListX<T>> groupedWhile(Predicate<? super T> predicate) {
        
        return stream(stream().groupedWhile(predicate));
    }

    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> groupedWhile(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return stream(stream().groupedWhile(predicate,factory));
    }

    @Override
    default <C extends Collection<? super T>> MutableCollectionX<C> groupedUntil(Predicate<? super T> predicate,
            Supplier<C> factory) {
        
        return stream(stream().groupedUntil(predicate,factory));
    }

    @Override
    default MutableCollectionX<ListX<T>> groupedStatefullyWhile(BiPredicate<ListX<? super T>, ? super T> predicate) {
        return stream(stream().groupedStatefullyWhile(predicate));
    }
    
	
	
}
