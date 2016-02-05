package com.aol.cyclops.lambda.monads;

import java.util.stream.Stream;

import lombok.val;

public class IsMonad {

	public boolean isMonad(Object t){
		return Stream.of(t.getClass().getMethods())
				.filter(method -> "flatMap".equals(method.getName()) || "bind".equals(method.getName()))
				.filter(method -> method.getParameterCount()==1)
				.findFirst().isPresent();
		
	
	}
}
