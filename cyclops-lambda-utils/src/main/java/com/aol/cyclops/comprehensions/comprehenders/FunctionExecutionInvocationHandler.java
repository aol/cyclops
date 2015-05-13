package com.aol.cyclops.comprehensions.comprehenders;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Function;

import lombok.Setter;

public class FunctionExecutionInvocationHandler implements InvocationHandler{

	@Setter
	private Function function;

	@Override
	public Object invoke(Object proxy, Method method, Object[] args)
			throws Throwable {
		return function.apply(args[0]);
	}
}
