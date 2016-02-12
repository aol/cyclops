package com.aol.cyclops.value;

import com.aol.cyclops.types.StreamableValue;

/**
 * Corerce an Object to implement the StreamableValue interface
 * 
 * This adds - support for decomposition, pattern matching, restreamable behaviour &amp; for comprehensions to your objects
 * 
 * @author johnmcclean
 *
 */
public class AsStreamableValue {
	
	/**
	 * Coerce / wrap an Object as a StreamableValue instance
	 * Adds pattern matching and decomposability
	 * As well as the ability to convert the fields of the supplied
	 * Object into a Stream
	 * 
	 * @param toCoerce Object to making into a StreamableValue
	 * @return StreamableValue that adds functionality to the supplied object
	 */
	public static <T> StreamableValue<T> asStreamableValue(Object toCoerce){
		return new CoercedStreamableValue<T>(toCoerce);
	}
	
	@lombok.Value
	public static class CoercedStreamableValue<T> implements StreamableValue<T>{
		private final Object v;
		
		public Object getMatchable(){
			return v;
		}
		public  Object unwrap(){
			return v;
		}
		public Object getStreamable(){
			return v;
		}
	}
}
