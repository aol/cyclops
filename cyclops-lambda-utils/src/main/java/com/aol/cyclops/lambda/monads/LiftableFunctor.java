package com.aol.cyclops.lambda.monads;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.stream.Stream;

import com.aol.cyclops.lambda.utils.ExceptionSoftener;

public interface LiftableFunctor<T,A,X extends LiftableFunctor<T,?,?>> extends Functor<T> {

	
	default X of(A current){
		
		try {
			Constructor cons = this.getClass().getConstructor(Object.class);
			cons.setAccessible(true);
			return (X) cons.newInstance(current);
		} catch (InstantiationException | IllegalAccessException
				| IllegalArgumentException | InvocationTargetException
				| NoSuchMethodException | SecurityException e) {
			ExceptionSoftener.singleton.factory.getInstance().throwSoftenedException(e);
		}
		return null;
	}
}
