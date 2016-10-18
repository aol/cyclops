package com.aol.cyclops.util.function;

import java.util.function.Consumer;
import java.util.function.Function;

import org.jooq.lambda.function.Consumer3;
import org.jooq.lambda.function.Consumer4;

/**
 * A FunctionalInterface for side-effecting statements that accept 4 inputs (with no result).
 * The four-arity specialization of {@link Consumer}.
 * 
 * @author johnmcclean
 *
 * @param <T1> Type of first input parameter
 * @param <T2> Type of second input parameter
 * @param <T3> Type of third input parameter
 * @param <T4> Type of fourth input parameter
 */
@FunctionalInterface
public interface QuadConsumer<T1, T2, T3, T4> {

    /**
     * Create a cyclops-react QuadConsumer from a jOOλ Consumer4
     * @param c4 jOOλ Consumer4
     * @return cyclops-react QuadConsumer
     */
    static <S1, S2, S3, S4> QuadConsumer<S1, S2, S3, S4> fromConsumer3(Consumer4<S1, S2, S3, S4> c4) {
        return (a, b, c, d) -> c4.accept(a, b, c, d);
    }

    /**
     * Performs operation with input parameters
     * 
     * @param a the first input parameter
     * @param b the second input parameter
     * @param c the third input parameter
     * @param d the fourth input parameter
     */
    void accept(T1 a, T2 b, T3 c, T4 d);

    /**
     * @return A jOOλ Consumer4
     */
    default Consumer4<T1, T2, T3, T4> consumer4() {
        return (a, b, c, d) -> accept(a, b, c, d);
    }

    /**
     * Partially apply the first input parameter to this QuadConsumer
     * 
     * @param s the first input parameter
     * @return A curried function that eventually resolves to a Consumer
     */
    default Function<T2, Function<T3, Consumer<T4>>> apply(T1 s) {
        return CurryConsumer.curryC4(this)
                            .apply(s);
    }

    /**
     * Partially apply the first and second input parameters to this QuadConsumer
     * 
     * @param s the first input parameter
     * @param s2 the second input parameter
     * @return A curried function that eventually resolves to a Consumer
     */
    default Function<T3, Consumer<T4>> apply(T1 s, T2 s2) {
        return CurryConsumer.curryC4(this)
                            .apply(s)
                            .apply(s2);
    }

    /**
     * Partially apply the first, second and third input parameters to this QuadConsumer
     * 
     * @param s the first input parameter
     * @param s2 the second input parameter
     * @param s3 the third input parameter
     * @return A consumer of the final value
     */
    default Consumer<T4> apply(T1 s, T2 s2, T3 s3) {
        return CurryConsumer.curryC4(this)
                            .apply(s)
                            .apply(s2)
                            .apply(s3);
    }
}
