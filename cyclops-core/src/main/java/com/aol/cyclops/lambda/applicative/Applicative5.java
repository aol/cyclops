package com.aol.cyclops.lambda.applicative;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.lambda.monads.ConvertableFunctor;
import com.aol.cyclops.lambda.monads.Functor;

@FunctionalInterface
public interface Applicative5<T,T2,T3,T4,T5,R, D extends ConvertableFunctor<R>> extends 
				Functor<Function<? super T,Function<? super T2, Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>> {

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> Functor<U> map(
			Function<? super Function<? super T, Function<? super T2,  Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>, ? extends U> fn) {
		return delegate().map(fn);
	}

	//<U extends Functor<Function<? super T,? extends R>> & Convertable<Function<? super T,? extends R>>> U delegate();
	ConvertableFunctor<Function<? super T,Function<? super T2, Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>>  delegate();
	
	default Applicative4<T2,T3,T4,T5,R,D> ap(Functor<T> f){
		Function<? super T,Function<? super T2, Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>> fn = delegate().get();
		
		return ()->(ConvertableFunctor)f.map(t->fn.apply(t));
		
	}
	default Applicative4<T2,T3,T4,T5,R,D> ap(Optional<T> f){
		
		Function<? super T,Function<? super T2, Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>> fn = delegate().get();
		
		return ()->(ConvertableFunctor)Maybe.fromOptional(f).map(t->fn.apply(t));
		
	}
	default Applicative4<T2,T3,T4,T5,R,D> ap(CompletableFuture<T> f){
		
		Function<? super T,Function<? super T2, Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>> fn = delegate().get();
		
		return ()->(ConvertableFunctor)new FutureW<>(f).map(t->fn.apply(t));
		
	}
}
