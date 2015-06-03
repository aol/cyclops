package com.aol.cyclops.lambda.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.aol.cyclops.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.lambda.monads.Monad;
import com.aol.cyclops.lambda.monads.MonadWrapper;

public interface AsGenericMonad {

	/**
	 * Create a duck typed Monad. 
	 * Monaad should have methods
	 * 
	 * {@code 
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
	 * @param monad to wrap
	 * @return Duck typed Monad
	 */
	public static <MONAD,T> Monad<MONAD,T> asMonad(Object monad){
		return new MonadWrapper<>(monad);
	}
	public static <T> Monad<Stream<T>,T> monad(Streamable<T> monad){
		return new MonadWrapper<>(monad);
	}
	public static <T> Monad<Stream<T>,T> monad(Stream<T> monad){
		return new MonadWrapper<>(monad);
	}
	public static <T> Monad<Optional<T>,T> monad(Optional<T> monad){
		return new MonadWrapper<>(monad);
	}
	public static <T> Monad<CompletableFuture<T>,T> monad(CompletableFuture<T> monad){
		return new MonadWrapper<>(monad);
	}
	public static <T> Monad<Stream<T>,T> monad(Collection<T> monad){
		return convertToMonad(monad);
	}
	public static <T> Monad<Stream<T>,T> monad(Iterable<T> monad){
		return convertToMonad(monad);
	}
	public static <T> Monad<Stream<T>,T> monad(Iterator<T> monad){
		return convertToMonad(monad);
	}
	public static <T> Monad<Stream<T>,T> monad(T... values){
		return monad(Stream.of(values));
	}
	public static <T> Monad<?,T> toMonad(Object monad){
		return new MonadWrapper<>(monad);
	}
	public static <T,MONAD> Monad<T,MONAD> convertToMonad(Object monad){
		return new MonadWrapper<>(new MonadicConverters().convertToMonadicForm(monad));
	}
}
