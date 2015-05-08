package com.aol.cyclops.comprehensions;

import java.util.function.Supplier;

public class LessTypingForComprehension2<T,R> {
	
	
	public static interface Step1<T,R>{
		public  Step2<T,R> flatMapAs$1(Object f);
		public <T> T $1();
		public <T> T $2();
		
	}
	public static interface Step2<T,R>{
		public  Step3<T,R> mapAs$2(Object f);
		public  Step3<T,R> mapAs$2(Supplier f);
		public  Step2<T,R> filter(Supplier<Boolean> s);
		
	}
	
	public static interface Step3<T,R>{
		public  Step4<T,R> filter(Supplier<Boolean> s);
		public <R> R yield(Supplier s);
		public void run(Runnable r);
		
	}
	public static interface Step4<T,R>{
		public <R> R yield(Supplier s);
		public void run(Runnable r);
		
	}


}

