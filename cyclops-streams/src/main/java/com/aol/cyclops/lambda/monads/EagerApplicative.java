package com.aol.cyclops.lambda.monads;

import java.util.function.Function;

import com.aol.cyclops.closures.mutable.Mutable;


import lombok.AccessLevel;
import lombok.AllArgsConstructor;

/**
 * Applicative for Eager Functors (with an arity of 1)
 * 
 * @author johnmcclean
 *
 * @param <T>
 * @param <R>
 */
public interface EagerApplicative<T,R> extends Functor<Function<? super T,? extends R>>{

	
	static <T,R> EagerApplicative<T,R> of(Functor<Function<? super T,? extends R>> functor){
		return new ApplicativeImpl<T,R>(functor);
	}
	
	
	default<T2> Functor<R> ap(Functor<T> f){
		EagerApplicative<T,R> parent = this;
		return f.<R>map(t->{
			Mutable<Function<? super T,? extends R>> mut = Mutable.of(null);
			parent.peek(t2-> {
				mut.set(t2);
			});
			return mut.get().apply(t);
		});
	}
	@AllArgsConstructor(access=AccessLevel.PRIVATE)
	public static class ApplicativeImpl<T,R> implements EagerApplicative<T,R>{
		Functor<Function<? super T,? extends R>> delegate;

		@Override
		public <U> Functor<U> map(Function<? super Function<? super T, ? extends R>, ? extends U> fn) {
			return delegate.map(fn);
		}
	}
}
