package com.aol.cyclops.invokedynamic;


import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionSoftener {

	
	
	
	public static RuntimeException throwSoftenedException(final Throwable e) {
		throw ExceptionSoftener.<RuntimeException>uncheck(e);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T uncheck(Throwable throwable) throws T {
		throw (T) throwable;
	}
			 
			
	

}

