package com.aol.cyclops.collections.extensions.standard;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.aol.cyclops.collections.extensions.CollectionX;
import com.aol.cyclops.trampoline.Trampoline;

public interface MutableCollectionX<T> extends CollectionX<T> {
	
	<X> CollectionX<X> fromStream(Stream<X> stream);
	
	default CollectionX<T> reverse(){
		return fromStream(stream().reverse()); 
	}
	default CollectionX<T> filter(Predicate<? super T> pred){
		return fromStream(stream().filter(pred));
	}
	default <R> CollectionX<R> map(Function<? super T, ? extends R> mapper){
		return fromStream(stream().map(mapper));
	}
	default <R> CollectionX<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper){
		return fromStream(stream().flatMap(mapper));
	}
	default CollectionX<T> limit(long num){
		return fromStream(stream().limit(num));
	}
	default CollectionX<T> skip(long num){
		return fromStream(stream().skip(num));
	}
	default CollectionX<T> takeWhile(Predicate<? super T> p){
		return fromStream(stream().limitWhile(p));
	}
	default CollectionX<T> dropWhile(Predicate<? super T> p){
		return fromStream(stream().skipWhile(p));
	}
	default CollectionX<T> takeUntil(Predicate<? super T> p){
		return fromStream(stream().limitUntil(p));
	}
	default CollectionX<T> dropUntil(Predicate<? super T> p){
		return fromStream(stream().skipUntil(p));
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
		
		 return  fromStream(stream().trampoline(mapper));	 
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#slice(long, long)
	 */
	default CollectionX<T> slice(long from, long to){
		return fromStream(stream().slice(from,to));	 
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.jooq.lambda.Seq#sorted(java.util.function.Function)
	 */
	default <U extends Comparable<? super U>> CollectionX<T> sorted(Function<? super T, ? extends U> function){
		return fromStream(stream().sorted(function));
	}
	default CollectionX<T> plus(T e){
		add(e);
		return this;
	}
	
	default CollectionX<T> plusAll(Collection<? extends T> list){
		addAll(list);
		return this;
	}
	
	default CollectionX<T> minus(Object e){
		remove(e);
		return this;
	}
	
	default CollectionX<T> minusAll(Collection<?> list){
		removeAll(list);
		return this;
	}
}
