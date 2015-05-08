package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step1;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step3;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step4;



public class LessTypingForComprehension4<T,R> {
	
	public static interface Step1<T,R>{
		public  Step2<T,R> flatMapAs$1(Object f);
		public <T> T $1();
		public <T> T $2();
		public <T> T $3();
		public <T> T $4();
		
	}
	public static interface Step2<T,R>{
		public  Step3<T,R> flatMapAs$2(Object f);
		public  Step3<T,R> flatMapAs$2(Supplier f);
		public  Step2<T,R> filter(Supplier<Boolean> s);
		
	}
	public static interface Step3<T,R>{
		public  Step4<T,R> flatMapAs$3(Object f);
		public  Step4<T,R> flatMapAs$3(Supplier f);
		public  Step3<T,R> filter(Supplier<Boolean> s);
		
		
	}
	public static interface Step4<T,R>{
		public  Step5<T,R> mapAs$4(Object f);
		public  Step5<T,R> mapAs$4(Supplier f);
		public  Step3<T,R> filter(Supplier<Boolean> s);
		
		
	}
	public static interface Step5<T,R>{
		public  Step4<T,R> filter(Supplier<Boolean> s);
		public <R> R yield(Supplier s);
		public void run(Runnable r);
		
	}
	public static interface Step6<T,R>{
		public <R> R yield(Supplier s);
		public void run(Runnable r);
		
	}


}
