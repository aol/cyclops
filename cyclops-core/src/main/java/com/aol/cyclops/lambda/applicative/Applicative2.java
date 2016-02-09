package com.aol.cyclops.lambda.applicative;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.functions.inference.Lambda;
import com.aol.cyclops.lambda.monads.ConvertableFunctor;
import com.aol.cyclops.lambda.monads.Functor;

import fj.data.Stream;

@FunctionalInterface
public interface Applicative2<T,T2,R, D extends ConvertableFunctor<R>> extends Functor<Function<? super T,Function<? super T2,? extends R>>> {

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> Functor<U> map(
			Function<? super Function<? super T, Function<? super T2, ? extends R>>, ? extends U> fn) {
		return delegate().map(fn);
	}

	//<U extends Functor<Function<? super T,? extends R>> & Convertable<Function<? super T,? extends R>>> U delegate();
	ConvertableFunctor<Function<? super T,Function<? super T2,? extends R>>>  delegate();
	
	default Applicative<T2,R,D> ap(Functor<T> f){
		return ()->(ConvertableFunctor)delegate().toOptional().map (myFn-> f.map(t->myFn.apply(t))).orElse(Maybe.none());
		
	}
	default Applicative<T2,R,D> ap(Optional<T> f){
		
		return ap(Maybe.fromOptional(f));
		
		
	}
	default Applicative<T2,R,D> ap(CompletableFuture<T> f){
		return ap(FutureW.of(f));
		
	}
}
