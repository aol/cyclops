package com.aol.cyclops.lambda.types.applicative;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import com.aol.cyclops.control.FutureW;
import com.aol.cyclops.control.Maybe;
import com.aol.cyclops.lambda.types.ConvertableFunctor;
import com.aol.cyclops.lambda.types.Functor;

@FunctionalInterface
public interface Applicative3<T,T2,T3,R, D extends ConvertableFunctor<R>> extends Functor<Function<? super T,Function<? super T2, Function<? super T3,? extends R>>>> {

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> Functor<U> map(
			Function<? super Function<? super T, Function<? super T2,  Function<? super T3,? extends R>>>, ? extends U> fn) {
		return delegate().map(fn);
	}

	//<U extends Functor<Function<? super T,? extends R>> & Convertable<Function<? super T,? extends R>>> U delegate();
	ConvertableFunctor<Function<? super T,Function<? super T2, Function<? super T3,? extends R>>>>  delegate();
	
	default Applicative2<T2,T3,R,D> ap(Functor<T> f){
		return ()->(ConvertableFunctor)delegate().toOptional().map (myFn-> f.map(t->myFn.apply(t))).orElse(Maybe.none());
	}
	default Applicative2<T2,T3,R,D> ap(Optional<T> f){	
		return ap(Maybe.fromOptional(f));
	}
	default Applicative2<T2,T3,R,D> ap(CompletableFuture<T> f){
		return ap(FutureW.of(f));
	}
}
