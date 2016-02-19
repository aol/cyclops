package com.aol.cyclops.types;

public interface Unwrapable {
	default  <R> R  unwrap(){
		return (R)this;
	}
}
