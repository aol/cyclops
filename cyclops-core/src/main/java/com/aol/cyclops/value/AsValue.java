package com.aol.cyclops.value;


public class AsValue {
	public static Value asValue(Object toCoerce){
		return new CoercedValue(toCoerce);
	}
	@lombok.Value
	public static class CoercedValue implements Value{
		private final Object value;
		
		public Object getMatchable(){
			return value;
		}
			
	}
}
