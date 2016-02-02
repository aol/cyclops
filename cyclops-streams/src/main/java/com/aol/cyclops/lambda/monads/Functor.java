package com.aol.cyclops.lambda.monads;

import java.util.function.Consumer;
import java.util.function.Function;


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
	
	
}
