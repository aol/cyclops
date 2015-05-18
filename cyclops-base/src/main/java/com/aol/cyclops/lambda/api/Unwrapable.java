package com.aol.cyclops.lambda.api;

public interface Unwrapable {
	default Object unwrap(){
		return this;
	}
}
