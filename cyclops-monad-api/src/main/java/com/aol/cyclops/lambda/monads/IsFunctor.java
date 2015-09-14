package com.aol.cyclops.lambda.monads;

import java.lang.reflect.Method;
import java.util.function.Function;
import java.util.stream.Stream;

public class IsFunctor {
	
	public boolean map(Object t) {
		return Stream.of(t.getClass().getMethods())
				.filter(method -> "map".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1).findFirst().isPresent();
	}
}
