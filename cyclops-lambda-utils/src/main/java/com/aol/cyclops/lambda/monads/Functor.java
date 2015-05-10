package com.aol.cyclops.lambda.monads;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.comprehensions.comprehenders.Comprehenders;

import lombok.AllArgsConstructor;
import lombok.experimental.Wither;



/**
 * Trait that encapsulates any Functor type
 * Uses reflection to apply JDK 8 java.util.Function
 * @author johnmcclean
 *
 * @param <T>
 */
//@AllArgsConstructor
public interface Functor<T> {
	//@Wither
//	private final Object functor;
	public <T> Functor<T> withFunctor(Object functor);
	public Object getFunctor();
	default  <R> Functor<R>  map(Function<T,R> fn) {
		return withFunctor((T)new ComprehenderSelector().selectComprehender(Comprehenders.Companion.instance.getComprehenders(),
				getFunctor())
				.map(getFunctor(), fn));
	
	}
	default   Functor<T>  peek(Consumer<T> c) {
		return (Functor)map(input -> {
			c.accept(input);
			return  input;
		});
	}
	default <X> X get(){
		return (X)getFunctor();
	}
	
}
