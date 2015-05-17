package com.aol.cyclops.functions;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class CurryConsumer {
	
	public static <T1,T2> Function<T1,Consumer<T2>> curry2(BiConsumer<T1,T2> biCon){
		return  t1 -> t2 -> biCon.accept(t1,t2);
	}
	public static <T1,T2,T3> Function<T1,Function<T2,Consumer<T3>>> curry3(TriConsumer<T1,T2,T3> triCon){
		return  t1-> t2 -> t3 -> triCon.accept(t1,t2,t3);
	}
	public static <T1,T2,T3,T4> Function<T1,Function<T2,Function<T3,Consumer<T4>>>> curry4(QuadConsumer<T1,T2,T3,T4> quadCon){
		return  t1-> t2 -> t3 -> t4 ->quadCon.accept(t1,t2,t3,t4);
	}
	public static <T1,T2,T3,T4,T5> Function<T1,Function<T2,Function<T3,Function<T4,Consumer<T5>>>>> curry5(QuintConsumer<T1,T2,T3,T4,T5> quintCon){
		return  t1-> t2 -> t3 -> t4 ->t5-> quintCon.accept(t1,t2,t3,t4,t5);
	}
	
}
