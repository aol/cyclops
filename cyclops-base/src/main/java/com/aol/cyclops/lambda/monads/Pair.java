package com.aol.cyclops.lambda.monads;

import lombok.Value;

@Value
public class Pair<T1,T2> {

	T1 _1;
	T2 _2;
	
	public T1 _1(){
		return _1;
	}
	public T2 _2(){
		return _2;
	}
	
}
