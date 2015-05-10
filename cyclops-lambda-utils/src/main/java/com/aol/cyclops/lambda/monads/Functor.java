package com.aol.cyclops.lambda.monads;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

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
		Method m = Stream.of(getFunctor().getClass().getMethods())
				.filter(method -> "map".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1).findFirst()
				.get();
		m.setAccessible(true);
		Class z = m.getParameterTypes()[0];
		Object o = Proxy.newProxyInstance(Functor.class
				.getClassLoader(), new Class[]{z}, (proxy,
				method, args) -> {
			return fn.apply((T)args[0]);
		});

		try {
			return (Functor)withFunctor(m.invoke(getFunctor(), o));
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
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
	/**
	public static <T> Functor<T> of(Object of) {
		return new Functor(of);
	}
	**/
}
