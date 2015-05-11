package com.aol.cyclops.comprehensions.functions;

import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

public class UncurryConsumer {
	
	public static <T1,T2> BiConsumer<T1,T2>  uncurry2(Function<T2,Consumer<T1>> biCon){
		return   (t1,t2) -> biCon.apply(t2).accept(t1);
	}
	public static <T1,T2,T3>  TriConsumer<T1,T2,T3>  uncurry3(Function<T3,Function<T2,Consumer<T1>>> triCon){
		return  (t1,t2,t3) -> triCon.apply(t3).apply(t2).accept(t1);
	}
	public static <T1,T2,T3,T4> QuadConsumer<T1,T2,T3,T4>  uncurry4(Function<T4,Function<T3,Function<T2,Consumer<T1>>>> quadCon){
		return  (t1,t2,t3,t4) -> quadCon.apply(t4).apply(t3).apply(t2).accept(t1);
	}
	public static <T1,T2,T3,T4,T5> QuintConsumer<T1,T2,T3,T4,T5>  uncurry5(Function<T5,Function<T4,Function<T3,Function<T2,Consumer<T1>>>>> quintCon){
		return  (t1,t2,t3,t4,t5) -> quintCon.apply(t5).apply(t4).apply(t3).apply(t2).accept(t1);
	}
	
}
