package com.aol.cyclops.collections.extensions.persistent;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.collections.extensions.FluentCollectionX;
import com.aol.cyclops.sequence.Monoid;
import com.aol.cyclops.trampoline.Trampoline;

public interface PersistentCollectionX<T> extends FluentCollectionX<T>{

	<T> Monoid<? extends Collection<T>> monoid();
	
	default CollectionX<T> reverse(){
		return from(this.<T>monoid().mapReduce(stream().reverse())); 
	}
	default CollectionX<T> filter(Predicate<? super T> pred){
		return from(this.<T>monoid().mapReduce(stream().filter(pred)));
	}
	default <R> CollectionX<R> map(Function<? super T, ? extends R> mapper){
		return from(this.<R>monoid().mapReduce(stream().map(mapper)));
	}
	default <R> CollectionX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper){
		return from(this.<R>monoid().mapReduce(stream().flatMap(mapper)));
	}
	default CollectionX<T> limit(long num){
		return from(this.<T>monoid().mapReduce(stream().limit(num)));
	}
	default CollectionX<T> skip(long num){
		return from(this.<T>monoid().mapReduce(stream().skip(num)));
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
		
		 return  from(this.<R>monoid().mapReduce(stream().trampoline(mapper)));	 
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
}
