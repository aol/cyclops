package com.aol.cyclops.closures.immutable;

import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.lambda.applicative.Applicativable;
import com.aol.cyclops.lambda.monads.Unit;
import com.aol.cyclops.sequence.SequenceM;
import com.aol.cyclops.value.Value;

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
@ToString
public class LazyImmutable<T> implements Supplier<T>, Consumer<T>, Value<T>, Applicativable<T>{
	private final static Object UNSET = new Object();
	private AtomicReference value = new AtomicReference<>(UNSET);
	private final AtomicBoolean set= new AtomicBoolean(false);
	
	public LazyImmutable(){}

	/**
	 * @return Current value
	 */
	public T get(){
		return (T)value.get();
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
	public <R> LazyImmutable<R> map(Function<? super T,? extends R> fn){
		T val = get();
		if(val==UNSET)
			return (LazyImmutable)this;
		else
			return LazyImmutable.of(fn.apply(val));
	}
	
	/**
	 * FlatMap the value stored in Immutable Closed Value from one Value to another
	 *  If this is an unitiatilised ImmutableClosedValue, an uninitialised closed value will be returned instead
	 * 
	 * @param fn  Flat Mapper function
	 * @return new ImmutableClosedValue with new mapped value 
	 */
	public <R> LazyImmutable<? extends R> flatMap(Function<? super T,? extends LazyImmutable<? extends R>> fn){
		
		T val = get();
		if(val==UNSET)
			return (LazyImmutable)this;
		else
			return fn.apply(val);
	}
	/**
	 * 
	 * Set the value of this ImmutableClosedValue
	 * If it has already been set will throw an exception
	 * 
	 * @param val Value to set to
	 * @return Current set Value
	 */
	public LazyImmutable<T> setOnce(T val){
		this.value.compareAndSet(UNSET, val);
		set.set(true);
		return this;
			
	}
	private  T setOnceFromSupplier(Supplier<T> lazy){
		
		this.value.compareAndSet(UNSET, lazy.get());
		return (T)this.value.get();	

	}
	/**
	 * Get the current value or set if it has not been set yet
	 * 
	 * @param lazy Supplier to generate new value
	 * @return Current value
	 */
	public T computeIfAbsent(Supplier<T> lazy) {
		T val = get();
		if(val==UNSET)
			return setOnceFromSupplier(lazy);
		
		return val;
		
	}

	@Override
	public void accept(T t) {
		setOnce(t);
		
	}

	@Override
	public SequenceM<T> stream() {
		return SequenceM.generate(this).limit(1);
	}

	@Override
	public Iterator<T> iterator() {
		return stream().iterator();
	}

	@Override
	public <T> Unit<T> unit(T unit) {
		return LazyImmutable.of(unit);
	}
}
