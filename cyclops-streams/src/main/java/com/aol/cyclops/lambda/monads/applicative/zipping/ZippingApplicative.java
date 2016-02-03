package com.aol.cyclops.lambda.monads.applicative.zipping;

import java.util.Iterator;
import java.util.function.Function;

import com.aol.cyclops.lambda.monads.ConvertableFunctor;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.IterableFunctor;
import com.aol.cyclops.sequence.SequenceM;

@FunctionalInterface
public interface ZippingApplicative<T,R, D extends IterableFunctor<R>> extends Functor<Function<? super T,? extends R>> {

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> IterableFunctor<U> map(Function<? super Function<? super T, ? extends R>, ? extends U> fn) {
		return delegate().map(fn);
	}

	//<U extends Functor<Function<? super T,? extends R>> & Convertable<Function<? super T,? extends R>>> U delegate();
	IterableFunctor<Function<? super T,? extends R>>  delegate();
	
	default<T2> D ap(IterableFunctor<T> f){
		Iterator<Function<? super T,? extends R>> fn = delegate().iterator();
		Iterator<T> it = f.iterator();
		return (D) delegate().unitIterator(SequenceM.fromIterator(fn).cycle().zip(SequenceM.fromIterator(it))
								 .map(t->t.v1.apply(t.v2)).iterator());
		
		
	}
}
