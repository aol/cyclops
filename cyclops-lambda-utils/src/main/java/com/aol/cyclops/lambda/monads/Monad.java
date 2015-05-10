package com.aol.cyclops.lambda.monads;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.api.Comprehender;



/**
 * Trait that encapsulates any Monad type
 * A generalised view into Any Monad (that implements flatMap or bind and accepts any function definition
 * with an arity of 1).
 * 
 * @author johnmcclean
 *
 * @param <T>
 * @param <MONAD>
 */
public interface Monad<T,MONAD> extends Functor<T>{
	//call out to Reflection comprehender.
	
	public <T,MONAD> Monad<T,MONAD> withMonad(Object invoke);
	public Object getMonad();
	
	default <T> Monad<T,MONAD> withFunctor(Object functor){
		return withMonad(functor);
	}
	default Object getFunctor(){
		return getMonad();
	}
	
	default  <R> Monad<R,MONAD> map(Function<T,R> fn){
		return (Monad)Functor.super.map(fn);
	}
	default   Monad<T,MONAD>  peek(Consumer<T> c) {
		return (Monad)Functor.super.peek(c);
	}
	
	default <R,NT> Monad<NT,R> bind(Function<T,R> fn){
		Method m = Stream.of(getMonad().getClass().getMethods())
				.filter(method -> "flatMap".equals(method.getName()) || "bind".equals(method.getName()) )
				.filter(method -> method.getParameterCount()==1)
				.findFirst().get();
		m.setAccessible(true);
		Class z = m.getParameterTypes()[0];
		
		Object o = Proxy.newProxyInstance(Monad.class
				.getClassLoader(), new Class[]{z}, (proxy,
				method, args) -> {
			return fn.apply((T)args[0]); //need to plug into Comprehender make safe mechanism here for mixed types
		});
		

		try {
			return (Monad) withMonad(m.invoke(getMonad(), o));
		} catch (IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			
			throw new RuntimeException(e);
		}
	}

	default <R extends MONAD,NT> Monad<NT,R> flatMap(Function<T,R> fn) {
		
		return bind(fn);
	}
	default  <T> T get(){
		return (T)getMonad();
	}
	

}
