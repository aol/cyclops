package com.aol.cyclops.lambda.api;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

import com.aol.cyclops.comprehensions.converters.MonadicConverters;
import com.aol.cyclops.lambda.monads.MonadWrapper;
import com.aol.cyclops.lambda.monads.Simplex;


public interface AsSimplexMonad {

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
	 * @param simplex to wrap
	 * @return Duck typed Monad
	 */
	public static <MONAD,T> Simplex<T> asSimplex(Object simplex){
		return new MonadWrapper<>(simplex).simplex();
	}
	public static <T> Simplex<T> simplex(Streamable<T> simplex){
		return new MonadWrapper<>(simplex).simplex();
	}
	public static <T> Simplex<T> simplex(Stream<T> simplex){
		return new MonadWrapper<>(simplex).simplex();
	}
	public static <T> Simplex<T> simplex(Optional<T> simplex){
		return new MonadWrapper<>(simplex).simplex();
	}
	public static <T> Simplex<T> simplex(CompletableFuture<T> simplex){
		return new MonadWrapper<>(simplex).simplex();
	}
	public static <T> Simplex<T> simplex(Collection<T> simplex){
		return convertToSimplex(simplex);
	}
	public static <T> Simplex<T> simplex(Iterable<T> simplex){
		return convertToSimplex(simplex);
	}
	public static <T> Simplex<T> simplex(Iterator<T> simplex){
		return convertToSimplex(simplex);
	}
	public static <T> Simplex<T> simplex(T... values){
		return simplex(Stream.of(values)).simplex();
	}
	public static <T> Simplex<T> toMonad(Object simplex){
		return new MonadWrapper<>(simplex).simplex();
	}
	public static <T> Simplex<T> convertToSimplex(Object simplex){
		return new MonadWrapper<>(new MonadicConverters().convertToMonadicForm(simplex)).simplex();
	}
}
