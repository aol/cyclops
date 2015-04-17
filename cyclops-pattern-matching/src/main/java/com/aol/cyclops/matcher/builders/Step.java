package com.aol.cyclops.matcher.builders;

public interface Step<T,R>{
	R thenApply(T t);
	default void thenConsume(T t){
		thenApply(t);
	}
}