package com.aol.cyclops.lambda.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.aol.cyclops.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.lambda.monads.MonadWrapper;
import com.aol.cyclops.lambda.monads.AnyM;


public interface AsAnyM {

	/**
	 * Create a duck typed Monad. 
	 * Monaad should have methods
	 * 
	 * <pre>{@code 
	 * map(F f)
	 * filter(P p)
	 * flatMap(F<x,MONAD> fm)
	 * }
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.	 
	 * Where P is a Functional Interface of any type that takes a single parameter and returns
	 * a boolean
	 * 
	 *  flatMap operations on the duck typed Monad can return any Monad type
	 * 
	 * @param anyM to wrap
	 * @return Duck typed Monad
	 */
	public static <MONAD,T> AnyM<T> asSimplex(Object anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	public static <T> AnyM<T> anyM(Streamable<T> anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	public static <T> AnyM<T> anyM(Stream<T> anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	public static <T> AnyM<T> anyM(Optional<T> anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	public static <T> AnyM<T> anyM(CompletableFuture<T> anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	public static <T> AnyM<T> anyM(Collection<T> anyM){
		return convertToSimplex(anyM);
	}
	public static <T> AnyM<T> anyM(Iterable<T> anyM){
		return convertToSimplex(anyM);
	}
	public static <T> AnyM<T> anyM(Iterator<T> anyM){
		return convertToSimplex(anyM);
	}
	public static <T> AnyM<T> anyM(T... values){
		return anyM(Stream.of(values)).anyM();
	}
	public static <T> AnyM<T> toMonad(Object anyM){
		return new MonadWrapper<>(anyM).anyM();
	}
	public static <T> AnyM<T> convertToSimplex(Object anyM){
		return new MonadWrapper<>(new MonadicConverters().convertToMonadicForm(anyM)).anyM();
	}
}
