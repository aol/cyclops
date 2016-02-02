package com.aol.cyclops.lambda.monads;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.hamcrest.Matcher;

import com.aol.cyclops.matcher.Case;
import com.aol.cyclops.matcher.Cases;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.trampoline.Trampoline;


/* 
 * @author johnmcclean
 *
 * @param <T>
 */

public interface Functor<T> {

	
	/**
	 * Cast all elements in a stream to a given type, possibly throwing a
	 * {@link ClassCastException}.
	 * 
	 * 
	 * // ClassCastException SequenceM.of(1, "a", 2, "b", 3).cast(Integer.class)
	 * 
	 */
	default <U> Functor<U> cast(Class<U> type){
		return map(type::cast);
	}
	<R> Functor<R>  map(Function<? super T,? extends R> fn);
	
	default   Functor<T>  peek(Consumer<? super T> c) {
		return (Functor)map(input -> {
			c.accept(input);
			return  input;
		});
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
	default <R> Functor<R> trampoline(Function<? super T, ? extends Trampoline<? extends R>> mapper){
		return  map(in-> mapper.apply(in).result());
	 }
	
	
	default <R> Functor<Optional<R>> when(Case<T,R,Function<T,R>>... cases){
		return map(t->Cases.of(cases).<R>match(t));
	}
	
	
}
