package com.aol.cyclops.value;


public class AsValue {
	
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
