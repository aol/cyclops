package com.aol.cyclops.matcher2;

import com.aol.cyclops.lambda.types.Decomposable;

import lombok.Value;

public class AsDecomposable {
	
	
	/**
	 * Coerce / wrap an Object as a Decomposable instance
	 * This adds an unapply method that returns an interable over the supplied
	 * objects fields.
	 * 
	 * Can be useful for pattern matching against object fields
	 * 
	 * 
	 * @param toCoerce Object to convert into a Decomposable
	 * @return Decomposable that delegates to the supplied object
	 */
	public static  Decomposable asDecomposable(Object toCoerce){
		return new CoercedDecomposable(toCoerce);
	}
	@Value
	public static class CoercedDecomposable implements Decomposable{
		private final Object dValue;
		public Object unwrap(){
			return dValue;
		}
		
	}
}
