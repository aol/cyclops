package com.aol.cyclops.lambda.monads;

import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;


/* 
 * @author johnmcclean
 *
 * @param <T>
 */

public interface BiFunctor<T1,T2> {

	
	
	<R1,R2> BiFunctor<R1,R2>  bimap(Function<? super T1,? extends R1> fn1,Function<? super T2,? extends R2> fn2);
	
	default   BiFunctor<T1,T2>  bipeek(Consumer<? super T1> c1, Consumer<? super T2> c2) {
		return (BiFunctor)bimap(input -> {
			c1.accept(input);
			return  input;
		},input -> {
			c2.accept(input);
			return  input;
		});
	}
	
	
}
