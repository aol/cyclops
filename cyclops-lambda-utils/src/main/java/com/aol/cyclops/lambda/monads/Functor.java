package com.aol.cyclops.lambda.monads;

import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;




/**
 * Trait that wraps & encapsulates any Functor type
 * Uses InvokeDynamic to call Map if no suitable Comprehender present
 * Uses (cached) JDK Dynamic Proxies to coerce function types to java.util.Function
 * @author johnmcclean
 *
 * @param <T>
 */

public interface Functor<T> {

	
	default <T> Functor<T> withFunctor(Object functor){
		return (Functor)this;
	}
	default Object getFunctor(){
		return this;
	}
	
	default  <R> Functor<R>  map(Function<T,R> fn) {
		return withFunctor((T)new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				getFunctor())
				.map(getFunctor(), fn));
	
	}
	default   Functor<T>  peek(Consumer<T> c) {
		return (Functor)map(input -> {
			c.accept(input);
			return  input;
		});
	}
	default <X> X unwrap(){
		return (X)getFunctor();
	}
	
}
