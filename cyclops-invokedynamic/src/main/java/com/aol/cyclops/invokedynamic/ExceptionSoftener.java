package com.aol.cyclops.invokedynamic;

import java.io.IOException;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionSoftener {

<<<<<<< HEAD
	public static <T extends Throwable,R> R throwSoftenedException(final T e) {
		uncheck(e);
		return null;
	}

	private static <T extends Throwable> void uncheck(Throwable throwable) throws T {
		throw (T) throwable;

=======
	
	
	
	public static RuntimeException throwSoftenedException(final Throwable e) {
		throw ExceptionSoftener.<RuntimeException>uncheck(e);
	}
	
	@SuppressWarnings("unchecked")
	private static <T extends Throwable> T uncheck(Throwable throwable) throws T {
		throw (T) throwable;
>>>>>>> 541bfbd5f5ab031ffed5125fc0061194c124f340
	}
	
	public static Integer myTest(){
		return throwSoftenedException(new IOException());
	}

}
