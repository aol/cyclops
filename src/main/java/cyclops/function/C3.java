package cyclops.function;

import java.util.function.Consumer;
import java.util.function.Function;



/**
 * A FunctionalInterface for side-effecting statements that accept 3 inputs (with no result).
 * The three-arity specialization of {@link Consumer}.
 * 
 * @author johnmcclean
 *
 * @param <S1> Type of first input parameter
 * @param <S2> Type of second input parameter
 * @param <S3> Type of third input parameter
 */
@FunctionalInterface
public interface C3<S1, S2, S3> {



    /**
     * Performs operation with input parameters
     *
     * @param a the first input parameter
     * @param b the second input parameter
     * @param c the third input parameter
     */
    void accept(S1 a, S2 b, S3 c);



    /**
     * Partially applyHKT the first input parameter to this C3
     * 
     * @param s the first input parameter
     * @return A curried function that returns a Consumer
     */
    default Function<S2, Consumer<S3>> apply(final S1 s) {
        return CurryConsumer.curryC3(this)
                            .apply(s);
    }

    /**
     * Partially applyHKT the first and second input parameter to this C3
     * 
     * @param s the first input parameter
     * @param s2 the second input parameter
     * @return A Consumer that accepts the third parameter
     */
    default Consumer<S3> apply(final S1 s, final S2 s2) {
        return CurryConsumer.curryC3(this)
                            .apply(s)
                            .apply(s2);
    }
}
