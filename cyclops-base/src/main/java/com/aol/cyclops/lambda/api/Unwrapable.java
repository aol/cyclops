package com.aol.cyclops.lambda.api;

public interface Unwrapable {
	default  <R> R  unwrap(){
		return (R)this;
	}
}
