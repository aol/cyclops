package com.aol.cyclops.lambda.monads;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;
import com.aol.cyclops.lambda.utils.ExceptionSoftener;




/**
 * Trait that wraps & encapsulates any Functor type
 * Uses InvokeDynamic to call Map if no suitable Comprehender present
 * Uses (cached) JDK Dynamic Proxies to coerce function types to java.util.Function
 * @author johnmcclean
 *
 * @param <T>
 */

public interface Functor<T> {

	default <T> Functor<T> withFunctor(Object functor){
		
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
	
	default Object getFunctor(){
		return this;
	}
	
	default  <R> Functor<R>  map(Function<T,R> fn) {
		Object value = new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				getFunctor()).map(getFunctor(), fn);
	
		return withFunctor((T)value);
	
	}
	default   Functor<T>  peek(Consumer<T> c) {
		return (Functor)map(input -> {
			c.accept(input);
			return  input;
		});
	}
	default <X> X unwrap(){
		if(getFunctor() instanceof Functor)
			return (X)((Functor)getFunctor()).unwrap();
		return (X)getFunctor();
	}
	
}
