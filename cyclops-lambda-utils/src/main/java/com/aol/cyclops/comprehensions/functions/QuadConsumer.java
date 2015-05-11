package com.aol.cyclops.comprehensions.functions;

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
}
