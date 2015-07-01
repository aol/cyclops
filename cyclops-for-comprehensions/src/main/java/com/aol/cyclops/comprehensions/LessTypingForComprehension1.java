package com.aol.cyclops.comprehensions;

import java.util.function.Consumer;
import java.util.function.Function;


@Deprecated //use com.aol.cyclops.comprehensions.donotation.typed.Do instead
public class LessTypingForComprehension1<T,R> {
	
	public static interface Vars1<T1> {
		public   T1 $1();
	}
	
	public static VarsImpl getVars(){
		return new VarsImpl();
	}
	public static class VarsImpl<T1> extends Varsonly<T1,Object,Object,Object,Object> implements Vars1<T1>{}
	
	public static interface Step1<T,R>{
		public  Step2<T,R> mapAs$1(Object f);
	}

	public static interface Step2<T,R>{
		public  <T1> Step3<T,R,T1> filter(Function<Vars1<T1>,Boolean> s);
		public <T1,R> R yield(Function<Vars1<T1>,?> s);
		public  <T1> Void run(Consumer<Vars1<T1>> r);
		
	}
	public static interface Step3<T,R,T1>{
		public <R> R yield(Function<Vars1<T1>,?> s);
		public  Void run(Consumer<Vars1<T1>> r);
		
	}

}
