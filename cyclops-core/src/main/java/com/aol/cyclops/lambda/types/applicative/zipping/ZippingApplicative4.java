package com.aol.cyclops.lambda.types.applicative.zipping;

import java.util.Iterator;
import java.util.function.Function;

import com.aol.cyclops.lambda.types.ConvertableFunctor;
import com.aol.cyclops.lambda.types.Functor;
import com.aol.cyclops.lambda.types.IterableFunctor;
import com.aol.cyclops.sequence.SequenceM;

import fj.data.Stream;

@FunctionalInterface
public interface ZippingApplicative4<T,T2,T3,T4,R, D extends IterableFunctor<R>> extends 
					Functor<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,? extends R>>>>> {

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> Functor<U> map(
			Function<? super Function<? super T, Function<? super T2, Function<? super T3,Function<? super T4,? extends R>>>>, ? extends U> fn) {
		return delegate().map(fn);
	}

	//<U extends Functor<Function<? super T,? extends R>> & Convertable<Function<? super T,? extends R>>> U delegate();
	IterableFunctor<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,? extends R>>>>>  delegate();
	
	default ZippingApplicative3<T2,T3,T4,R,D> ap(IterableFunctor<T> f){
	
		Iterator<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,? extends R>>>>> fn = delegate().iterator();
		Iterator<T> it = f.iterator();
		return ()-> (IterableFunctor)delegate().unitIterator(SequenceM.fromIterator(fn).zip(SequenceM.fromIterator(it))
								 .map(t->t.v1.apply(t.v2)).iterator());
		
	}
}
