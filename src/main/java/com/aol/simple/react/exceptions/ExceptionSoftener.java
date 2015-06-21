package com.aol.simple.react.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
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
