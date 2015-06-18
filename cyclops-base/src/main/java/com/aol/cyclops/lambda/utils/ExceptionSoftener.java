package com.aol.cyclops.lambda.utils;


import lombok.AllArgsConstructor;
import lombok.Getter;


public class ExceptionSoftener {

	@Getter @AllArgsConstructor
	public static enum singleton { factory( new  ExceptionSoftener ());
		
		private final ExceptionSoftener instance;
	    
	}
	
	
	public void throwSoftenedException(final Throwable e) {
		new Thrower<RuntimeException>().uncheck(e);
	}
	static class Thrower<T extends Throwable> {
		@SuppressWarnings("unchecked")
			private void uncheck(Throwable throwable) throws T {
			 	throw (T) throwable;
			 }
	}
			 
			
	

}

