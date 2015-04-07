package com.aol.cyclops;

import java.util.function.BiFunction;
import java.util.function.Function;

import javaslang.Functions.λ1;
import javaslang.Functions.λ2;

public interface Lambda<T,R> {
	public   R λ(T fn);
	
}
