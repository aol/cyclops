package com.aol.cyclops.comprehensions;

import java.util.function.Supplier;



public class LessTypingForComprehension1<T,R> {
	
	public static interface Step1<T,R>{
		public  Step2<T,R> mapAs$1(Object f);
		public <T> T $1();
	
	}

	public static interface Step2<T,R>{
		public  Step3<T,R> filter(Supplier<Boolean> s);
		public <R> R yield(Supplier s);
		public void run(Runnable r);
		
	}
	public static interface Step3<T,R>{
		public <R> R yield(Supplier s);
		public void run(Runnable r);
		
	}

}
