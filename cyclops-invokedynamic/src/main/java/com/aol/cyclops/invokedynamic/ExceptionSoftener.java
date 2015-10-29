package com.aol.cyclops.invokedynamic;

import java.io.IOException;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionSoftener {

	public static <T extends Throwable,R> R throwSoftenedException(final T e) {
		uncheck(e);
		return null;
	}

	private static <T extends Throwable> void uncheck(Throwable throwable) throws T {
		throw (T) throwable;

	}
	
	public static Integer myTest(){
		return throwSoftenedException(new IOException());
	}

}
