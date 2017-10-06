package cyclops.function;

import java.util.function.Consumer;
import java.util.function.Function;


/**
 * A FunctionalInterface for side-effecting statements that accept 5 inputs (with no result).
 * The five-arity specialization of {@link Consumer}.
 * 
 * @author johnmcclean
 *
 * @param <T1> Type of first input parameter
 * @param <T2> Type of second input parameter
 * @param <T3> Type of third input parameter
 * @param <T4> Type of fourth input parameter
 * @param <T5> Type of fifth input parameter
 */
@FunctionalInterface
public interface C5<T1, T2, T3, T4, T5> {



    /**
     * Performs operation with input parameters
     * 
     * @param a the first input parameter
     * @param b the second input parameter
     * @param c the third input parameter
     * @param d the fourth input parameter
     * @param e the fifth input parameter
     */
    void accept(T1 a, T2 b, T3 c, T4 d, T5 e);



    /**
     * Partially applyHKT the first input parameter to this C5
     * 
     * @param s the first input parameter
     * @return A curried function that eventually resolves to a Consumer
     */
    default Function<T2, Function<T3, Function<T4, Consumer<T5>>>> apply(final T1 s) {
        return CurryConsumer.curryC5(this)
                            .apply(s);
    }

    /**
     * Partially applyHKT the first and second input parameters to this C5
     * 
     * @param s the first input parameter
     * @param s2 the second input parameter
     * @return A curried function that eventually resolves to a Consumer
     */
    default Function<T3, Function<T4, Consumer<T5>>> apply(final T1 s, final T2 s2) {
        return CurryConsumer.curryC5(this)
                            .apply(s)
                            .apply(s2);
    }

    /**
     *  Partially applyHKT the first, second and third input parameters to this C5
     * 
     * @param s the first input parameter
     * @param s2 the second input parameter
     * @param s3 the third input parameter
     * @return A curried function that eventually resolves to a Consumer
     */
    default Function<T4, Consumer<T5>> apply(final T1 s, final T2 s2, final T3 s3) {
        return CurryConsumer.curryC5(this)
                            .apply(s)
                            .apply(s2)
                            .apply(s3);
    }

    /**
     * 
     * Partially applyHKT the first, second, third and fourth input parameters to this C5
     * 
     * @param s the first input parameter
     * @param s2 the second input parameter
     * @param s3 the third input parameter
     * @param s4 the fourth input parameter
     * @return A consumer of the final value
     */
    default Consumer<T5> apply(final T1 s, final T2 s2, final T3 s3, final T4 s4) {
        return CurryConsumer.curryC5(this)
                            .apply(s)
                            .apply(s2)
                            .apply(s3)
                            .apply(s4);
    }
}
