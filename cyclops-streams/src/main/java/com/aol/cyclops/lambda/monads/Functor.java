package com.aol.cyclops.lambda.monads;

import java.util.function.Consumer;
import java.util.function.Function;


/* 
 * @author johnmcclean
 *
 * @param <T>
 */

public interface Functor<T> {

	
	
	<R> Functor<R>  map(Function<? super T,? extends R> fn);
	
	default   Functor<T>  peek(Consumer<? super T> c) {
		return (Functor)map(input -> {
			c.accept(input);
			return  input;
		});
	}
	
	
}
