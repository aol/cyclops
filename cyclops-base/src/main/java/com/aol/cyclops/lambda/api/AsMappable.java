package com.aol.cyclops.lambda.api;

import lombok.Value;

/**
 * Don't break encapsulation of classes for testing purposes
 * Coerce Objects to Map form in testing, to test their values.
 * 
 * @author johnmcclean
 *
 */
public class AsMappable {


	
	public static  Mappable asMappable(Object toCoerce){
		return new CoercedMappable(toCoerce);
	}
	@Value
	public static class CoercedMappable implements Mappable{
		private final Object dValue;
		public Object unwrap(){
			return dValue;
		}
		
		
	}
}
