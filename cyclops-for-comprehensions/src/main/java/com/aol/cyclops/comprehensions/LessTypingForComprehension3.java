package com.aol.cyclops.comprehensions;

import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step1;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step2AndAHalf;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step3;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Step4;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.Vars2;
import com.aol.cyclops.comprehensions.LessTypingForComprehension2.VarsImpl;



public class LessTypingForComprehension3<T,R> {

	public static interface Vars3<T1,T2,T3> {
		public  T1 $1();
		public  T2 $2();
		public  T3 $3();
	}
	
	public static VarsImpl getVars(){
		return new VarsImpl();
	}
	public static class VarsImpl<T1,T2,T3> extends Varsonly<T1,T2,T3,Object,Object> implements Vars3<T1,T2,T3>{}
	
	public static interface Step1<T,R>{
		public  Step2<T,R> flatMapAs$1(Object f);
		
	}
	public static interface Step2<T,R>{
		public   <T1,T2,T3> Step3<T,R,T1,T2,T3> flatMapAs$2(Function<Vars3<T1,T2,T3>,Object> f);
		public   <T1,T2,T3> Step2AndAHalf<T,R,T1,T2,T3> filter(Function<Vars3<T1,T2,T3>,Boolean> f);
		
	}
	public static interface Step2AndAHalf<T,R,T1,T2,T3>{
		public 	Step3<T,R,T1,T2,T3> flatMapAs$2(Function<Vars3<T1,T2,T3>,Object> f);
		public  Step2AndAHalf<T,R,T1,T2,T3> filter(Function<Vars3<T1,T2,T3>,Boolean> f);
		
	}
	public static interface Step3<T,R,T1,T2,T3>{
		public  Step4<T,R,T1,T2,T3> mapAs$3(Function<Vars3<T1,T2,T3>,Object> f);
		public  Step3<T,R,T1,T2,T3> filter(Function<Vars3<T1,T2,T3>,Boolean> f);
		
		
	}
	public static interface Step4<T,R,T1,T2,T3>{
		public Step5<T,R,T1,T2,T3> filter(Function<Vars3<T1,T2,T3>,Boolean> f);
		public <R> R yield(Function<Vars3<T1,T2,T3>,?> s);
		public  Void run(Consumer<Vars3<T1,T2,T3>> r);
		
	}
	public static interface Step5<T,R,T1,T2,T3>{
		public <R> R yield(Function<Vars3<T1,T2,T3>,?> s);
		public Void run(Consumer<Vars3<T1,T2,T3>> r);
		
	}

}
