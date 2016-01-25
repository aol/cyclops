package com.aol.cyclops.functions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.Wither;

import com.aol.cyclops.functions.caching.Cacheable;
import com.aol.cyclops.functions.caching.Memoize;
import com.aol.cyclops.functions.currying.Curry;
import com.aol.cyclops.functions.currying.PartialApplicator;

public class Functions {
	
	public static <T,R> Functions.f1<T,R> of(Function<T,R> fn){
		
		return new f1<>(fn);
	}
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class s<R> implements Supplier<R>{
		private final Supplier<R> fn;
		
		@Override
		public R get(){
			return fn.get();
		}
		
		public s<R> around(Function<Advice0<R>,R> around){
			return withFn(()->around.apply(new Advice0<R>(fn)));
		}
		
		public s<R> memoize(){
			return withFn(Memoize.memoizeSupplier(fn));
		}
		public s<R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeSupplier(fn,cache));
		}
		
		public s<Optional<R>> lift(){
			return new s<>(() -> Optional.ofNullable(fn.get()));
		}
		/**
		public s<AnyM<R>> liftM(){
			return new f1<>(()-> opt.map(t->fn.apply(t)));
		}
		**/
		
	}
	
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class f1<T,R> implements Function<T,R>{
		Function<T,R> fn;
		@Override
		public R apply(T t) {
			return fn.apply(t);
		}
		
		public f1<T,R> around(Function<Advice1<T,R>,R> around){
			return withFn(t->around.apply(new Advice1<T,R>(t,fn)));
		}
		
		public s<R> partiallyApply(T param){
			return new s<>(PartialApplicator.partial(param,fn));
		}
		public f1<T,R> memoize(){
			return withFn(Memoize.memoizeFunction(fn));
		}
		public f1<T,R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeFunction(fn));
		}
		
		public f1<Optional<T>,Optional<R>> lift(){
			return new f1<>(opt -> opt.map(t->fn.apply(t)));
		}
		/**
		public f1<AnyM<T>,AnyM<R>> liftM(){
			return new f1<>(opt -> opt.map(t->fn.apply(t)));
		}
		**/
		
	}
	@Wither(AccessLevel.PRIVATE)
	@AllArgsConstructor
	public static class f2<T1,T2,R> implements BiFunction<T1,T2,R>{
		BiFunction<T1,T2,R> fn;
		@Override
		public R apply(T1 t1,T2 t2) {
			return fn.apply(t1,t2);
		}
		
		public f2<T1,T2,R> around(Function<Advice2<T1,T2,R>,R> around){
			return withFn((t1,t2)->around.apply(new Advice2<>(t1,t2,fn)));
		}
		
		public f1<T2,R> partiallyApply(T1 param){
			return new f1<>(PartialApplicator.partial2(param,fn));
		}
		public s<R> partiallyApply(T1 param1,T2 param2){
			return new s<>(PartialApplicator.partial2(param1,param2,fn));
		}
		public f1<T1,Function<T2,R>> curry(){
			return new f1<>(Curry.curry2(fn));
		}
		public f2<T1,T2,R> memoize(){
			return withFn(Memoize.memoizeBiFunction(fn));
		}
		public f2<T1,T2,R> memoize(Cacheable<R> cache){
			return withFn(Memoize.memoizeBiFunction(fn));
		}
		
		public f2<Optional<T1>,Optional<T2>,Optional<R>> lift(){
			return new f2<>((opt1,opt2) -> opt1.flatMap(t1-> opt2.map(t2->fn.apply(t1,t2))));
		}
		/**
		public f1<AnyM<T>,AnyM<R>> liftM(){
			return new f1<>(opt -> opt.map(t->fn.apply(t)));
		}
		**/
		
	}
	
	@AllArgsConstructor
	public static class Advice0<R>{
		
		private final Supplier<R> fn;
		
		public R proceed(){
			return fn.get();
		}
		
	}
	
	@AllArgsConstructor
	public static class Advice1<T,R>{
		public final T param;
		private final Function<T,R> fn;
		
		public R proceed(){
			return fn.apply(param);
		}
		public R proceed(T param){
			return fn.apply(param);
		}
	}
	@AllArgsConstructor
	public static class Advice2<T1,T2,R>{
		public final T1 param1;
		public final T2 param2;
		private final BiFunction<T1,T2,R> fn;
		
		public R proceed(){
			return fn.apply(param1,param2);
		}
		public R proceed(T1 param1, T2 param2){
			return fn.apply(param1,param2);
		}
		public R proceed1(T1 param){
			return fn.apply(param,param2);
		}
		public R proceed(T2 param){
			return fn.apply(param1,param);
		}
	}
}
