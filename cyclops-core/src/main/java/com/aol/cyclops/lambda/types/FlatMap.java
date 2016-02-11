package com.aol.cyclops.lambda.types;

import java.util.function.BiFunction;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.donotation.typed.Do;
import com.aol.cyclops.functions.CurryVariance;
import com.aol.cyclops.functions.TriFunction;
import com.aol.cyclops.monad.AnyM;

import lombok.Value;

public interface FlatMap<T> extends Functor<T>, ToAnyM<T> {

	
	public <R> FlatMap<R> flatten();
	
	
	
	default<T2> Apply2<T,T2> applyWith(Functor<T2> functor){
		return new Apply2Impl<T,T2>(anyM(),functor);
	}
	
	default<T2,T3>  Apply3<T,T2,T3> applyWith(FlatMap<T2> monad,Functor<T3> functor2){
		return new Apply3Impl<T,T2,T3>(anyM(),monad.anyM(),functor2);
	}
	public static interface Apply2<T1,T2>{
		public <R> AnyM<R> apply(Function<? super T1,Function<? super T2,? extends R>> apply);
		public <R> AnyM<R> apply(BiFunction<? super T1,? super T2,? extends R> fn);
	}
	public static interface Apply3<T1,T2,T3>{
		public <R>  AnyM<R> apply(Function<? super T1,Function<? super T2,Function<? super T3,? extends R>>> apply);
		public <R>  AnyM<R> apply(TriFunction<? super T1,? super T2,? super T3,? extends R> fn);
	}
	@Value
	public static class Apply2Impl<T1,T2> implements Apply2<T1,T2>{
		AnyM<T1> monad1;
		Functor<T2> functor2;
		public <R> AnyM<R> apply(Function<? super T1,Function<? super T2,? extends R>> apply){
			return Do.add(monad1)
					.add(AnyM.<T2>ofMonad(functor2)) //although only a functor we can make use of the map method safely
					.yield(apply);
		}
		public <R> AnyM<R> apply(BiFunction<? super T1,? super T2,? extends R> fn){
			return apply(CurryVariance.curry2(fn));
		}
	}
	@Value
	public static class Apply3Impl<T1,T2,T3> implements Apply3<T1,T2,T3>{
		AnyM<T1> monad1;
		AnyM<T2> monad2;
		Functor<T3> functor3;
		public <R> AnyM<R> apply(Function<? super T1,Function<? super T2,Function<? super T3,? extends R>>> apply){
			return Do.add(monad1)
					.add(monad2)
					.add(AnyM.<T3>ofMonad(functor3)) //although only a functor we can make use of the map method safely
					.yield(apply)
					.unwrap();
		}
		public <R> AnyM<R> apply(TriFunction<? super T1,? super T2,? super T3,? extends R> fn){
			return apply(CurryVariance.curry3(fn));
		}
	}
	
	//Maybe.of(10).applyWith(Maybe.of(20)).apply(this::add)
	
}
