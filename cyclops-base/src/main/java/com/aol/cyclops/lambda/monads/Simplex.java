package com.aol.cyclops.lambda.monads;

public interface Simplex<T> extends Monad<Object,T>{
	public <R> R monad();
}