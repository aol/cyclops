package com.aol.cyclops.sequence;

public interface Unwrapable {
	default  <R> R  unwrap(){
		return (R)this;
	}
}
