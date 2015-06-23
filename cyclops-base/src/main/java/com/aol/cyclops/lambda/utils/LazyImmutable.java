package com.aol.cyclops.lambda.utils;

import java.util.function.Function;
import java.util.function.Supplier;

import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * A class that represents an 'immutable' value that is generated inside a lambda
 * expression, but is accessible outside it
 * 
 * It will only allow it's value to be set once. Unfortunately the compiler won't be
 * able to tell if setOnce is called more than once
 * 
 * example usage
 * 
 * <pre>{@code
 * public static <T> Supplier<T> memoiseSupplier(Supplier<T> s){
		LazyImmutable<T> lazy = LazyImmutable.def();
		return () -> lazy.computeIfAbsent(s);
	}
 * }</pre>
 * 
 * Has map and flatMap methods, but is not a Monad (see example usage above for why, it is the initial mutation that is valuable).
 * 
 * @author johnmcclean
 *
 * @param <T>
 */
@ToString @EqualsAndHashCode
public class LazyImmutable<T> implements Supplier<T>{
	private T value;
	private boolean set=false;
	
	public LazyImmutable(){}
	/**
	 * @return Current value
	 */
	public T get(){
		return value;
	}
	/**
	 * Create an intermediate unbound (or unitialised) ImmutableClosedValue)
	 *
	 * @return unitialised ImmutableClosedValue
	 */
	public static <T> LazyImmutable<T> unbound(){
		return new LazyImmutable();
	}
	/**
	 * @param value Create an initialised ImmutableClosedValue with specified value
	 * @return Initialised ImmutableClosedValue
	 */
	public static <T> LazyImmutable<T> of(T value){
		LazyImmutable v =  new LazyImmutable();
		v.setOnce(value);
		return v;
	}
	
	/**
	 * @return a defined, but unitialised LazyImmutable
	 */
	public static <T> LazyImmutable<T> def(){
		return new LazyImmutable<>();
	}
	
	
	/**
	 * Map the value stored in this Immutable Closed Value from one Value to another
	 * If this is an unitiatilised ImmutableClosedValue, an uninitialised closed value will be returned instead
	 * 
	 * @param fn Mapper function
	 * @return new ImmutableClosedValue with new mapped value 
	 */
	public <R> LazyImmutable<R> map(Function<T,R> fn){
		if(!set)
			return (LazyImmutable)this;
		else
			return LazyImmutable.of(fn.apply(value));
	}
	
	/**
	 * FlatMap the value stored in Immutable Closed Value from one Value to another
	 *  If this is an unitiatilised ImmutableClosedValue, an uninitialised closed value will be returned instead
	 * 
	 * @param fn  Flat Mapper function
	 * @return new ImmutableClosedValue with new mapped value 
	 */
	public <R> LazyImmutable<R> flatMap(Function<T,LazyImmutable<R>> fn){
		if(!set)
			return (LazyImmutable)this;
		else
			return fn.apply(value);
	}
	/**
	 * 
	 * Set the value of this ImmutableClosedValue
	 * If it has already been set will throw an exception
	 * 
	 * @param val Value to set to
	 * @return Current set Value
	 */
	public synchronized LazyImmutable<T> setOnce(T val) throws LazyImmutableSetMoreThanOnceException{
		if(!this.set){
			this.value = val;
			this.set=true;
			return this;
		}
		throw new  LazyImmutableSetMoreThanOnceException("Current value " + this.value + " attempt to reset to " + val);
	}
	private synchronized T setOnceFromSupplier(Supplier<T> lazy){
		
		if(!this.set){
			this.value = lazy.get();
			this.set=true;
			return this.value;
		}
		
		return this.value;

	}
	/**
	 * Get the current value or set if it has not been set yet
	 * 
	 * @param lazy Supplier to generate new value
	 * @return Current value
	 */
	public T computeIfAbsent(Supplier<T> lazy) {
		if(set)
			return value;
		return setOnceFromSupplier(lazy);
	}
}
