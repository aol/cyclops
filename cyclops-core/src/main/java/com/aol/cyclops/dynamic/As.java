package com.aol.cyclops.dynamic;

import java.util.function.Supplier;
import java.util.stream.Stream;

import com.aol.cyclops.Monoid;
import com.aol.cyclops.control.Matchable;
import com.aol.cyclops.control.Trampoline;
import com.aol.cyclops.lambda.api.AsFunctor;
import com.aol.cyclops.lambda.api.AsGenericMonoid;
import com.aol.cyclops.lambda.api.AsMappable;
import com.aol.cyclops.lambda.api.AsSupplier;
import com.aol.cyclops.lambda.api.Mappable;
import com.aol.cyclops.lambda.types.Decomposable;
import com.aol.cyclops.lambda.types.Functor;
import com.aol.cyclops.matcher2.AsDecomposable;
import com.aol.cyclops.matcher2.AsMatchable;
import com.aol.cyclops.sequence.streamable.AsStreamable;
import com.aol.cyclops.sequence.streamable.Streamable;
import com.aol.cyclops.value.AsStreamableValue;
import com.aol.cyclops.value.AsValue;
import com.aol.cyclops.value.StreamableValue;
import com.aol.cyclops.value.ValueObject;

@Deprecated
public interface As {
	
	
	/**
	 * Wrap the object as a replayable Stream
	 * 
	 * @param toCoerce Object to wrap as a replayable Stream
	 * @return Replayable Stream
	 */
	public static <T> Streamable<T> asStreamableFromObject(Object toCoerce){
		return AsStreamable.fromObject(toCoerce);
	}
	/**
	 * Wrap the stream as a replayable Stream
	 * 
	 * @param toCoerce Stream to wrap as a replayable Stream
	 * @return Replayable Stream
	 */
	public static <T> Streamable<T> asStreamable(Stream<T> toCoerce){
		return AsStreamable.fromObject(toCoerce);
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
	 * @param method Method to call when Supplier.get() called
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
	
	/**
	 * Create a Trampoline that is completed
	 * 
	 * @param t Result value
	 * @return Completed Trampoline
	 */
	public static <T> Trampoline<T> asDone(T t){
		return Trampoline.done(t);
	}
	/**
	 * Create a Trampoline with more work to do
	 * 
	 * <pre>
	 * {@code
	 * 		return As.asMore(()->loop(times-1,sum+times));
	 * }</pre>
	 * 
	 * @param trampoline Next stage in computation
	 * @return In progress Trampoline
	 */
	public static <T> Trampoline<T> asMore(Trampoline<Trampoline<T>> trampoline){
		return Trampoline.more(trampoline);
	}
	
}
