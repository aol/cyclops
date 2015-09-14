package com.aol.cyclops.invokedynamic;


import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionSoftener {

	
	
	
	public static void throwSoftenedException(final Throwable e) {
		new Thrower<RuntimeException>().uncheck(e);
	}
	static class Thrower<T extends Throwable> {
		@SuppressWarnings("unchecked")
			private void uncheck(Throwable throwable) throws T {
			 	throw (T) throwable;
			 }
	}
			 
			
	

}

