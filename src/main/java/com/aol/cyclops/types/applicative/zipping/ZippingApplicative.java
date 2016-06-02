package com.aol.cyclops.types.applicative.zipping;

import java.util.Iterator;
import java.util.function.Function;

import com.aol.cyclops.control.ReactiveSeq;
import com.aol.cyclops.types.ConvertableFunctor;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.types.IterableFunctor;

@FunctionalInterface
public interface ZippingApplicative<T,R, D extends IterableFunctor<R>> extends Functor<Function<? super T,? extends R>> {

	/* (non-Javadoc)
	 * @see com.aol.cyclops.lambda.monads.Functor#map(java.util.function.Function)
	 */
	@Override
	default <U> IterableFunctor<U> map(Function<? super Function<? super T, ? extends R>, ? extends U> fn) {
		return delegate().map(fn);
	}

	
	IterableFunctor<Function<? super T,? extends R>>  delegate();
	
	default<T2> D ap(Iterable<? extends T> f){
		Iterator<Function<? super T,? extends R>> fn = delegate().iterator();
		Iterator<? extends T> it = f.iterator();
		return (D) delegate().unitIterator(ReactiveSeq.fromIterator(fn).zip(ReactiveSeq.fromIterator(it))
								 .map(t->t.v1.apply(t.v2)).iterator());		
	}
}
