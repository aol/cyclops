package com.aol.cyclops.types.mixins;

import lombok.AllArgsConstructor;
import lombok.Value;

/**
 * Don't break encapsulation of classes for testing purposes
 * Coerce Objects to Map form in testing, to test their values.
 * 
 * @author johnmcclean
 *
 */
public class AsMappable {


	
	/**
	 * Convert supplied object to a Mappable instance.
	 * Mappable will convert the (non-static) fields of the supplied object into a map
	 * 
	 * 
	 * @param toCoerce Object to convert to a Mappable
	 * @return  Mappable instance
	 */
	public static  Mappable asMappable(Object toCoerce){
		return new CoercedMappable(toCoerce);
	}
	@AllArgsConstructor
	public static class CoercedMappable implements Mappable{
		private final Object dValue;
		public Object unwrap(){
			return dValue;
		}	
	}
}
