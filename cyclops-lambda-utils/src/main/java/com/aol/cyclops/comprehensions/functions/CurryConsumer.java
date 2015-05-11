package com.aol.cyclops.comprehensions.functions;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class CurryConsumer {
	
	public static <T1,T2> Function<T2,Consumer<T1>> curry2(BiConsumer<T1,T2> biCon){
		return  t2 -> t1 -> biCon.accept(t1,t2);
	}
	public static <T1,T2,T3> Function<T3,Function<T2,Consumer<T1>>> curry3(TriConsumer<T1,T2,T3> triCon){
		return  t3-> t2 -> t1 -> triCon.accept(t1,t2,t3);
	}
	public static <T1,T2,T3,T4> Function<T4,Function<T3,Function<T2,Consumer<T1>>>> curry4(QuadConsumer<T1,T2,T3,T4> quadCon){
		return  t4 -> t3-> t2 -> t1 -> quadCon.accept(t1,t2,t3,t4);
	}
	public static <T1,T2,T3,T4,T5> Function<T5,Function<T4,Function<T3,Function<T2,Consumer<T1>>>>> curry5(QuintConsumer<T1,T2,T3,T4,T5> quintCon){
		return  t5-> t4 -> t3-> t2 -> t1 -> quintCon.accept(t1,t2,t3,t4,t5);
	}
	
}
