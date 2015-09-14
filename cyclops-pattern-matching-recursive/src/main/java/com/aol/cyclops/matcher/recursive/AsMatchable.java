package com.aol.cyclops.matcher.recursive;

import lombok.AllArgsConstructor;

public class AsMatchable {
	
	
	/**
	 * Coerce / wrap an Object as a Matchable instance
	 * This adds match / _match methods for pattern matching against the object
	 * 
	 * @param toCoerce Object to convert into a Matchable
	 * @return Matchable that adds functionality to the supplied object
	 */
	public static  Matchable asMatchable(Object toCoerce){
		return new CoercedMatchable(toCoerce);
	}
	@AllArgsConstructor
	public static class CoercedMatchable implements Matchable{
		private final Object matchable;
		@Override
		public Object getMatchable(){
			return matchable;
		}
	}
}
