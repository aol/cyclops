package com.aol.cyclops.lambda.monads;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;




/**
 * An interoperability trait that wraps & encapsulates any Functor type
 * 
 * Uses InvokeDynamic to call Map if no suitable Comprehender present
 * Uses (cached) JDK Dynamic Proxies to coerce function types to java.util.Function
 * @author johnmcclean
 *
 * @param <T>
 */

public interface Functor<T> {

	/**
	 * Will attempt to create a new instance of this functor type via constructor reflection
	 * if this is a wrapped Functor (i.e. getFunctor returns another instance) otherwise
	 * returns the supplied functor
	 * 
	 * 
	 * @param functor
	 * @return
	 */
	default <T> Functor<T> withFunctor(T functor){
		if(getFunctor()==this)
			return (Functor<T>)functor;
		
		try {
			Constructor cons = this.getClass().getConstructor(Object.class);
			cons.setAccessible(true);
			return (Functor) cons.newInstance(functor);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
		}
		return null;
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
	
	default  <R> Functor<R>  map(Function<T,R> fn) {
		Object value = new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				getFunctor()).map(getFunctor(), fn);
	
		return withFunctor((R)value);
	
	}
	default   Functor<T>  peek(Consumer<T> c) {
		return (Functor)map(input -> {
			c.accept(input);
			return  input;
		});
	}
	default <X> X unwrap(){
		if(getFunctor()!=this && getFunctor() instanceof Functor)
			return (X)((Functor)getFunctor()).unwrap();
		return (X)getFunctor();
	}
	
}
