package com.aol.cyclops.functions;

import java.util.function.Consumer;
import java.util.function.Function;

import com.aol.cyclops.functions.currying.CurryConsumer;

/**
 * Functional Interface for currying operations
 * @author johnmcclean
 *
 * @param <T1>
 * @param <T2>
 * @param <T3>
 * @param <T4>
 */
public interface QuadConsumer<T1, T2, T3, T4> {

	public void accept(T1 a,T2 b,T3 c,T4 d);
	
	default Function<T2,Function<T3,Consumer<T4>>> apply(T1 s){
		return CurryConsumer.curryC4(this).apply(s);
	}
	default Function<T3,Consumer<T4>> apply(T1 s, T2 s2){
		return CurryConsumer.curryC4(this).apply(s).apply(s2);
	}
	default Consumer<T4> apply(T1 s, T2 s2,T3 s3){
		return CurryConsumer.curryC4(this).apply(s).apply(s2).apply(s3);
	}
}
