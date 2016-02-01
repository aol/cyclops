package com.aol.cyclops.collections.extensions.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

import org.jooq.lambda.Seq;
import org.jooq.lambda.tuple.Tuple2;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.FluentCollectionX;
import com.aol.cyclops.collections.extensions.standard.ListX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.streams.StreamUtils;
import com.aol.cyclops.trampoline.Trampoline;

public interface PersistentCollectionX<T> extends FluentCollectionX<T>{
	@Override
	default SequenceM<T> stream(){
		
		return SequenceM.fromIterable(this);
	}
	<R> PersistentCollectionX<R> emptyUnit();
	<T> Monoid<? extends Collection<T>> monoid();
	
	default CollectionX<T> reverse(){
		return from(this.<T>monoid().mapReduce(stream().reverse())); 
	}
	default CollectionX<T> filter(Predicate<? super T> pred){
		FluentCollectionX<T> mapped = emptyUnit();
		Iterator<T> it = iterator();
		while(it.hasNext()){
			T value  = it.next();
			if(pred.test(value))
				mapped = mapped.plusInOrder(value);
		}
		return unit(mapped);
	
	}
	default <R> CollectionX<R> map(Function<? super T, ? extends R> mapper){
		FluentCollectionX<R> mapped = emptyUnit();
		Iterator<T> it = iterator();
		while(it.hasNext())
			mapped = mapped.plusInOrder(mapper.apply(it.next()));
		return unit(mapped);
	}
	default <R> CollectionX<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper){
		return from(this.<R>monoid().mapReduce(stream().flatMap(mapper.andThen(StreamUtils::stream))));
	}
	default CollectionX<T> limit(long num){
		FluentCollectionX<T> mapped = emptyUnit();
		Iterator<T> it = iterator();
		for(long i=0;i<num && it.hasNext();i++){
			mapped = mapped.plusInOrder(it.next());
		}
		return mapped;
		
	}
	default CollectionX<T> skip(long num){
		FluentCollectionX<T> mapped = emptyUnit();
		Iterator<T> it = iterator();
		for(long i=0;i<num && it.hasNext();i++){
			it.next();
		}
		while(it.hasNext())
			mapped = mapped.plusInOrder(it.next());
		return mapped;
	//	return from(this.<T>monoid().mapReduce(stream().skip(num)));
	}
	default CollectionX<T> dropRight(int num){
		return from(this.<T>monoid().mapReduce(stream().skipLast(num)));
	}
	default CollectionX<T> takeRight(int num){
		return from(this.<T>monoid().mapReduce(stream().limitLast(num)));
	}
	default CollectionX<T> takeWhile(Predicate<? super T> p){
		return from(this.<T>monoid().mapReduce(stream().limitWhile(p)));
	}
	default CollectionX<T> dropWhile(Predicate<? super T> p){
		return from(this.<T>monoid().mapReduce(stream().skipWhile(p)));
	}
	default CollectionX<T> takeUntil(Predicate<? super T> p){
		return from(this.<T>monoid().mapReduce(stream().limitUntil(p)));
	}
	default CollectionX<T> dropUntil(Predicate<? super T> p){
		return from(this.<T>monoid().mapReduce(stream().skipUntil(p)));
	}
	 /**
	  * Performs a map operation that can call a recursive method without running out of stack space
	  * <pre>
	  * {@code
	  * SequenceM.of(10,20,30,40)
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
	  * SequenceM.of(10_000,200_000,3_000_000,40_000_000)
				 .trampoline(i-> fibonacci(i))
				 .forEach(System.out::println);
				 
				 
	  * completes successfully
	  * }
	  * 
	 * @param mapper
	 * @return
	 */
	default <R> CollectionX<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		return map(in-> mapper.apply(in).result()); 
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	default CollectionX<T> slice(long from, long to){
		return from(this.<T>monoid().mapReduce(stream().slice(from,to)));	 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	default <U extends Comparable<? super U>> CollectionX<T> sorted(Function<? super T, ? extends U> function){
		return from(this.<T>monoid().mapReduce(stream().sorted(function)));
	}
	
	
	
	default CollectionX<ListX<T>> grouped(int groupSize){
		return from(this.<ListX<T>>monoid().mapReduce(stream().grouped(groupSize).map(ListX::fromIterable)));
	}
	default <K, A, D> CollectionX<Tuple2<K, D>> grouped(Function<? super T, ? extends K> classifier, Collector<? super T, A, D> downstream){
		return from(this.<Tuple2<K, D>>monoid().mapReduce(stream().grouped(classifier)));
	}
	default <K> CollectionX<Tuple2<K, Seq<T>>> grouped(Function<? super T, ? extends K> classifier){
		return from(this.<Tuple2<K, Seq<T>>>monoid().mapReduce(stream().grouped(classifier)));
	}
	default <U> CollectionX<Tuple2<T, U>> zip(Iterable<U> other){
		return from(this.<Tuple2<T, U>>monoid().mapReduce(stream().zip(other)));
	}
	default CollectionX<ListX<T>> sliding(int windowSize){
		return from(this.<ListX<T>>monoid().mapReduce(stream().sliding(windowSize).map(ListX::of)));
	}
	default CollectionX<ListX<T>> sliding(int windowSize, int increment){
		return from(this.<ListX<T>>monoid().mapReduce(stream().sliding(windowSize,increment).map(ListX::of)));
	}
	default CollectionX<T> scanLeft(Monoid<T> monoid){
		
		return from(this.<T>monoid().mapReduce(stream().scanLeft(monoid)));
	}
	default CollectionX<T> scanRight(Monoid<T> monoid){
		return from(this.<T>monoid().mapReduce(stream().scanRight(monoid)));
	}
	default <U> CollectionX<U> scanLeft(U seed, BiFunction<U, ? super T, U> function){
		return from(this.<U>monoid().mapReduce(stream().scanLeft(seed,function)));
	}
	
	default <U> CollectionX<U> scanRight(U identity, BiFunction<? super T, U, U> combiner){
		return from(this.<U>monoid().mapReduce(stream().scanRight(identity,combiner)));
	}
}
