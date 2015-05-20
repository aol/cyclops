package com.aol.cyclops.dynamic;

import java.util.Arrays;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.AsDecomposable;
import com.aol.cyclops.lambda.api.AsFunctor;
import com.aol.cyclops.lambda.api.AsGenericMonad;
import com.aol.cyclops.lambda.api.AsGenericMonoid;
import com.aol.cyclops.lambda.api.AsMappable;
import com.aol.cyclops.lambda.api.AsStreamable;
import com.aol.cyclops.lambda.api.AsSupplier;
import com.aol.cyclops.lambda.api.Decomposable;
import com.aol.cyclops.lambda.api.Mappable;
import com.aol.cyclops.lambda.api.Monoid;
import com.aol.cyclops.lambda.api.Streamable;
import com.aol.cyclops.lambda.api.AsGenericMonoid.WrappedMonoid;
import com.aol.cyclops.lambda.api.AsStreamable.CoercedStreamable;
import com.aol.cyclops.lambda.api.AsSupplier.CoercedSupplier;
import com.aol.cyclops.lambda.monads.Functor;
import com.aol.cyclops.lambda.monads.Monad;
import com.aol.cyclops.matcher.AsMatchable;
import com.aol.cyclops.matcher.Matchable;
import com.aol.cyclops.value.AsStreamableValue;
import com.aol.cyclops.value.AsValue;
import com.aol.cyclops.value.StreamableValue;
import com.aol.cyclops.value.ValueObject;

public interface As {
	
	/**
	 * Wrap the object as a replayable Stream
	 * 
	 * @param toCoerce Object to wrap as a replayable Stream
	 * @return Replayable Stream
	 */
	public static <T> Streamable<T> asStreamable(Object toCoerce){
		return AsStreamable.asStreamable(toCoerce);
	}
	/**
	 * Wrap the stream as a replayable Stream
	 * 
	 * @param toCoerce Stream to wrap as a replayable Stream
	 * @return Replayable Stream
	 */
	public static <T> Streamable<T> asStreamable(Stream<T> toCoerce){
		return AsStreamable.asStreamable(toCoerce);
	}

	/**
	 * Coerce / wrap an Object as a StreamableValue instance
	 * Adds pattern matching and decomposability
	 * As well as the ability to convert the fields of the supplied
	 * Object into a Stream
	 * 
	 * @param toCoerce Object to making into a StreamableValue
	 * @return StreamableValue that adds functionality to the supplied object
	 */
	public static <T> StreamableValue<T> asStreamableValue(Object toCoerce){
		return AsStreamableValue.asStreamableValue(toCoerce);
	}
	/**
	 * Coerce an Object to implement the ValueObject interface
	 * Adds pattern matching and decomposability functionality
	 * 
	 * @param toCoerce Object to coerce
	 * @return ValueObject that adds functionality to the supplied object
	 */
	public static ValueObject asValue(Object toCoerce){
		return AsValue.asValue(toCoerce);
	}
	/**
	 * Coerce / wrap an Object as a Decomposable instance
	 * This adds an unapply method that returns an interable over the supplied
	 * objects fields.
	 * 
	 * Can be useful for pattern matching against object fields
	 * 
	 * 
	 * @param toCoerce Object to convert into a Decomposable
	 * @return Decomposable  that adds functionality to the supplied object
	 */
	public static  Decomposable asDecomposable(Object toCoerce){
		return AsDecomposable.asDecomposable(toCoerce);
	}
	
	/**
	 * Convert supplied object to a Mappable instance.
	 * Mappable will convert the (non-static) fields of the supplied object into a map
	 * 
	 * 
	 * @param toCoerce Object to convert to a Mappable
	 * @return  Mappable instance
	 */
	public static  Mappable asMappable(Object toCoerce){
		return AsMappable.asMappable(toCoerce);
	}
	
	/**
	 * Coerce / wrap an Object as a Matchable instance
	 * This adds match / _match methods for pattern matching against the object
	 * 
	 * @param toCoerce Object to convert into a Matchable
	 * @return Matchable that adds functionality to the supplied object
	 */
	public static  Matchable asMatchable(Object toCoerce){
		return AsMatchable.asMatchable(toCoerce);
	}

	/**
	 * Create a duck typed Monad. 
	 * Monaad should have methods
	 * 
	 * map(F f)
	 * filter(P p)
	 * flatMap(F<x,MONAD> fm)
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
	public static <T,MONAD> Monad<T,MONAD> asMonad(Object monad){
		return AsGenericMonad.asMonad(monad);
	}
	/**
	 * Create a Duck typed functor. Wrapped class should have a method
	 * 
	 * map(F f)
	 * 
	 * Where F is a Functional Interface of any type that takes a single parameter and returns
	 * a result.
	 * 
	 * @param o functor to wrap
	 * @return Duck typed functor
	 */
	public static <T> Functor<T> asFunctor(Object o){
		return AsFunctor.asFunctor(o);
	}
	/**
	 * Create a Duck typing  based Supplier
	 * 
	 * 
	 * 
	 * @param toCoerce Object to convert into a Supplier, 
	 * 		must have a non-void get() method
	 * @return Supplier that delegates to the supplied object
	 */
	public static <T>  Supplier<T> asSupplier(Object toCoerce){
		return AsSupplier.asSupplier(toCoerce);
	}
	
	/**
	 * Create a Duck typing  based Supplier
	 * That returns the result of a call to the supplied method name
	 * 
	 * @param toCoerce Object to convert into a supplier
	 * @param methods Method to call when Supplier.get() called
	 * @return Supplier that delegates to supplied object
	 */
	public static <T>  Supplier<T> asSupplier(Object toCoerce, String method){
		return AsSupplier.asSupplier(toCoerce,method);
	}
	/**
	 * Wrap supplied Monoid object in the cylops Monoid interface
	 * 
	 * Will look for sum(a,b) or combine(a,b) methods for combiner
	 * and zero() method for zero
	 * 
	 * @param o Monoid type to wrap
	 * @return Cyclopse Monoid
	 */
	public static <A> Monoid<A> asMonoid(Object o){
		return AsGenericMonoid.asMonoid(o);
	}
	
	
}
