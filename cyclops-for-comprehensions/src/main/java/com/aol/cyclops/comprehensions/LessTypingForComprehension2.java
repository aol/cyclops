package com.aol.cyclops.comprehensions;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import com.aol.cyclops.comprehensions.LessTypingForComprehension1.Vars1;

@Deprecated //use com.aol.cyclops.comprehensions.donotation.typed.Do instead
public class LessTypingForComprehension2<T,R> {
	
	public static interface Vars2<T1,T2> {
		public T1 $1();
		public T2 $2();
	}
	
	public static VarsImpl getVars(){
		return new VarsImpl();
	}
	public static class VarsImpl<T1,T2> extends Varsonly<T1,T2,Object,Object,Object> implements Vars2<T1,T2>{}
	
	public static interface Step1<T,R>{
		public  Step2<T,R> flatMapAs$1(Object f);
		
		
	}
	public static interface Step2<T,R>{
		public  <T1,T2> Step3<T,R,T1,T2> mapAs$2(Function<Vars2<T1,T2>,Object> f);
		public  <T1,T2> Step2AndAHalf<T,R,T1,T2> filter(Function<Vars2<T1,T2>,Boolean> f);
		
	}
	public static interface Step2AndAHalf<T,R,T1,T2>{
		public Step3<T,R,T1,T2> mapAs$2(Function<Vars2<T1,T2>,Object> f);
		public  Step2AndAHalf<T,R,T1,T2> filter(Function<Vars2<T1,T2>,Boolean> f);
		
	}
	
	public static interface Step3<T,R,T1,T2>{
		public Step4<T,R,T1,T2> filter(Function<Vars2<T1,T2>,Boolean> f);
		public <R> R yield(Function<Vars2<T1,T2>,?> s);
		public Void run(Consumer<Vars2<T1,T2>> r);
		
	}
	public static interface Step4<T,R,T1,T2>{
		public <R> R yield(Function<Vars2<T1,T2>,?> s);
		public Void run(Consumer<Vars2<T1,T2>> r);
		
	}


}

