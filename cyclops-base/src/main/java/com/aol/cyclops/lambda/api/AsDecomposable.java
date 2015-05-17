package com.aol.cyclops.lambda.api;

import lombok.Value;

public class AsDecomposable {
	
	
	
	public static  Decomposable asDecomposable(Object toCoerce){
		return new CoercedDecomposable(toCoerce);
	}
	@Value
	public static class CoercedDecomposable implements Decomposable{
		private final Object dValue;
		
		
	}
}
