package com.aol.cyclops.lambda.monads.applicative;

import java.util.function.Function;

import com.aol.cyclops.lambda.monads.ConvertableFunctor;
import com.aol.cyclops.lambda.monads.Functor;

@FunctionalInterface
public interface Applicative<T,R, D extends Functor<R>> extends Functor<Function<? super T,? extends R>> {

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> Functor<U> map(Function<? super Function<? super T, ? extends R>, ? extends U> fn) {
		return delegate().map(fn);
	}

	//<U extends Functor<Function<? super T,? extends R>> & Convertable<Function<? super T,? extends R>>> U delegate();
	ConvertableFunctor<Function<? super T,? extends R>>  delegate();
	
	default<T2> D ap(Functor<T> f){
		Function<? super T,? extends R> fn = delegate().get();
		return (D)f.map(t->fn.apply(t));
		
	}
}
