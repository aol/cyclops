package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step1;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step3;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step4;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Vars2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.VarsImpl;
import com.aol.cyclops.comprehensions.LessTypingForComprehension3.Step2AndAHalf;


@Deprecated //use com.aol.cyclops.comprehensions.donotation.typed.Do instead
public class LessTypingForComprehension4<T,R> {

	public static interface Vars4<T1,T2,T3,T4> {
		public 	T1 $1();
		public  T2 $2();
		public  T3 $3();
		public  T4 $4();
	}
	
	public static VarsImpl getVars(){
		return new VarsImpl();
	}
	public static class VarsImpl<T1,T2,T3,T4> extends Varsonly<T1,T2,T3,T4,Object> implements Vars4<T1,T2,T3,T4>{}
	public static interface Step1<T,R>{
		public  Step2<T,R> flatMapAs$1(Object f);
		
	}
	public static interface Step2<T,R>{
		public <T1,T2,T3,T4> Step3<T,R,T1,T2,T3,T4> flatMapAs$2(Function<Vars4<T1,T2,T3,T4>,Object> f);
		public <T1,T2,T3,T4> Step2AndAHalf<T,R,T1,T2,T3,T4> filter(Function<Vars4<T1,T2,T3,T4>,Boolean> f);
		
	}
	public static interface Step2AndAHalf<T,R,T1,T2,T3,T4>{
		public 	Step3<T,R,T1,T2,T3,T4> flatMapAs$2(Function<Vars4<T1,T2,T3,T4>,Object> f);
		public  Step2AndAHalf<T,R,T1,T2,T3,T4> filter(Function<Vars4<T1,T2,T3,T4>,Boolean> f);
		
	}
	public static interface Step3<T,R,T1,T2,T3,T4>{
		public Step4<T,R,T1,T2,T3,T4> flatMapAs$3(Function<Vars4<T1,T2,T3,T4>,Object> f);
		public Step3<T,R,T1,T2,T3,T4> filter(Function<Vars4<T1,T2,T3,T4>,Boolean> f);
		
		
	}
	public static interface Step4<T,R,T1,T2,T3,T4>{
		public  Step5<T,R,T1,T2,T3,T4> mapAs$4(Function<Vars4<T1,T2,T3,T4>,Object> f);

		public Step4<T,R,T1,T2,T3,T4> filter(Function<Vars4<T1,T2,T3,T4>,Boolean> f);
		
		
	}
	public static interface Step5<T,R,T1,T2,T3,T4>{
		public  Step6<T,R,T1,T2,T3,T4> filter(Function<Vars4<T1,T2,T3,T4>,Boolean> f);
		public  R yield(Function<Vars4<T1,T2,T3,T4>,?> s);
		public  Void run(Consumer<Vars4<T1,T2,T3,T4>> r);
		
	}
	public static interface Step6<T,R,T1,T2,T3,T4>{
		public <R> R yield(Function<Vars4<T1,T2,T3,T4>,?> s);
		public Void run(Consumer<Vars4<T1,T2,T3,T4>> r);
		
	}


}
