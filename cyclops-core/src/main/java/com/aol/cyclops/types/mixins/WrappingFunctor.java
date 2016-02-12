package com.aol.cyclops.types.mixins;


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.lambda.monads.ComprehenderSelector;
import com.aol.cyclops.types.Functor;
import com.aol.cyclops.util.ExceptionSoftener;


/**
 * An interoperability trait that wraps &amp; encapsulates any Functor type
 * 
 * Uses InvokeDynamic to call Map if no suitable Comprehender present
 * Uses (cached) JDK Dynamic Proxies to coerce function types to java.util.Function
 * 
 * @author johnmcclean
 *
 * @param <T>
 */

public interface WrappingFunctor<T> extends Functor<T> {

	/**
	 * Will attempt to create a new instance of this functor type via constructor reflection
	 * if this is a wrapped Functor (i.e. getFunctor returns another instance) otherwise
	 * returns the supplied functor
	 * 
	 * 
	 * @param functor
	 * @return
	 */
	default <T> WrappingFunctor<T> withFunctor(T functor){
		if(getFunctor()==this)
			return (WrappingFunctor<T>)functor;
		
		try {
			Constructor cons = this.getClass().getConstructor(Object.class);
			cons.setAccessible(true);
			return (WrappingFunctor) cons.newInstance(functor);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			ExceptionSoftener.throwSoftenedException(e);
			return null;
		}
		
	}
	
	/**
	 * Override this method if you are using this class to wrap a Functor that does not
	 * implement this interface natively.
	 * 
	 * @return underlying functor
	 */
	default Object getFunctor(){
		return this;
	}
	
	default  <R> WrappingFunctor<R>  map(Function<? super T,? extends R> fn) {
		Object value = new ComprehenderSelector().selectComprehender(
				getFunctor()).map(getFunctor(), fn);
	
		return withFunctor((R)value);
	
	}
	default   WrappingFunctor<T>  peek(Consumer<? super T> c) {
		return (WrappingFunctor)map(input -> {
			c.accept(input);
			return  input;
		});
	}
	default <X> X unwrap(){
		if(getFunctor()!=this && getFunctor() instanceof Functor)
			return (X)((WrappingFunctor)getFunctor()).unwrap();
		return (X)getFunctor();
	}
	
}
