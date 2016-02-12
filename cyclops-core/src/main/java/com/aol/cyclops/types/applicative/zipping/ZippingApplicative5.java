package com.aol.cyclops.types.applicative.zipping;

import java.util.Iterator;
import java.util.function.Function;

import com.aol.cyclops.control.SequenceM;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.IterableFunctor;

import fj.data.Stream;

@FunctionalInterface
public interface ZippingApplicative5<T,T2,T3,T4,T5,R, D extends IterableFunctor<R>> extends 
					Functor<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>> {

	

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> Functor<U> map(
			Function<? super Function<? super T, Function<? super T2, Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>, ? extends U> fn) {
		return delegate().map(fn);
	}

	
	IterableFunctor<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>>  delegate();
	
	default ZippingApplicative4<T2,T3,T4,T5,R,D> ap(IterableFunctor<T> f){
	
		Iterator<Function<? super T,Function<? super T2,Function<? super T3,Function<? super T4,Function<? super T5,? extends R>>>>>> fn = delegate().iterator();
		Iterator<T> it = f.iterator();
		return ()-> (IterableFunctor)delegate().unitIterator(SequenceM.fromIterator(fn).zip(SequenceM.fromIterator(it))
								 .map(t->t.v1.apply(t.v2)).iterator());
		
	}
}
