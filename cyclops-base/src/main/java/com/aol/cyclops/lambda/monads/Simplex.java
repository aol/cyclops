package com.aol.cyclops.lambda.monads;

public interface Simplex<X> extends Monad<Object,X>{
	public <R> R monad();
}