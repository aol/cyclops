package com.aol.cyclops.value;


public class AsValue {
	
	/**
	 * Coerce an Object to implement the ValueObject interface
	 * Adds pattern matching and decomposability functionality
	 * 
	 * @param toCoerce Object to coerce
	 * @return ValueObject that delegates calls to the supplied object
	 */
	public static ValueObject asValue(Object toCoerce){
		return new CoercedValue(toCoerce);
	}
	@lombok.Value
	public static class CoercedValue implements ValueObject{
		private final Object value;
		public  Object unwrap(){
			return value;
		}
		
		public Object getMatchable(){
			return value;
		}
			
	}
}
