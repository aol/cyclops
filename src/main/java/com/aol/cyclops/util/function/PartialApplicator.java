package com.aol.cyclops.util.function;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class consists exclusively of static methods that return functions
 * that are partially applied with parameters.
 *
 * Note: Partial application has been done up to an arity of 8 so far.
 */

public class PartialApplicator extends Lambda {

    /**
    * Returns a function with 1 arguments applied to the supplied Function
    * @param t1 Generic argument
    * @param func Function
    * @param <T1> Generic argument type
    * @param <T2> Generic argument type
    
    * @param <R> Function generic return type
    * @return Function as a result of 2 arguments being applied to the incoming TriFunction
    */

    public static <T1, T2, R> Supplier<R> partial(final T1 t1, final Function<T1, R> func) {
        return () -> func.apply(t1);
    }

    /**
    * Returns a function with 2 arguments applied to the supplied BiFunction
    * @param t1 Generic argument
    * @param t2 Generic argument
    * @param biFunc Function that accepts 2 parameters
    * @param <T1> Generic argument type
    * @param <T2> Generic argument type
    
    * @param <R> Function generic return type
    * @return Function as a result of 2 arguments being applied to the incoming TriFunction
    */

    public static <T1, T2, R> Supplier<R> partial2(final T1 t1, final T2 t2, final BiFunction<T1, T2, R> biFunc) {
        return () -> biFunc.apply(t1, t2);
    }

    /**
     * Returns a function with 1 arguments applied to the supplied BiFunction
     * @param t1 Generic argument
     * @param biFunc Function that accepts 2 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
    
     * @param <R> Function generic return type
     * @return Function as a result of 2 arguments being applied to the incoming BiFunction
     */

    public static <T1, T2, R> Function<T2, R> partial2(final T1 t1, final BiFunction<T1, T2, R> biFunc) {
        return (t2) -> biFunc.apply(t1, t2);
    }
    /**
     * Returns a function with 1 arguments applied to the supplied BiFunction
     * @param t2 Generic argument
     * @param biFunc Function that accepts 2 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
    
     * @param <R> Function generic return type
     * @return Function as a result of 2 arguments being applied to the incoming BiFunction
     */

    public static <T1, T2, R> Function<T1, R> partial2b(final T2 t2, final BiFunction<T1, T2, R> biFunc) {
        return (t1) -> biFunc.apply(t1, t2);
    }

    /**
    * Returns a function with 3 arguments applied to the supplied TriFunction
    * @param t1 Generic argument
    * @param t2 Generic argument
    * @param triFunc Function that accepts 3 parameters
    * @param <T1> Generic argument type
    * @param <T2> Generic argument type
    * @param <T3> Generic argument type
    * @param <R> Function generic return type
    * @return Function as a result of 2 arguments being applied to the incoming TriFunction
    */

    public static <T1, T2, T3, R> Supplier<R> partial3(final T1 t1, final T2 t2, final T3 t3, final TriFunction<T1, T2, T3, R> triFunc) {
        return () -> triFunc.apply(t1, t2, t3);
    }

    /**
     * Returns a function with 2 arguments applied to the supplied TriFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param triFunc Function that accepts 3 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 2 arguments being applied to the incoming TriFunction
     */

    public static <T1, T2, T3, R> Function<T3, R> partial3(final T1 t1, final T2 t2, final TriFunction<T1, T2, T3, R> triFunc) {
        return (t3) -> triFunc.apply(t1, t2, t3);
    }

    /**
     * Returns a BiFunction with 1 argument applied to the supplied TriFunction
     * @param t1 Generic Argument
     * @param triFunc Function that accepts 3 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <R> BiFunction generic return type
     * @return BiFunction as a result of 1 argument being applied to the incoming TriFunction
     */

    public static <T1, T2, T3, R> BiFunction<T2, T3, R> partial3(final T1 t1, final TriFunction<T1, T2, T3, R> triFunc) {
        return (t2, t3) -> triFunc.apply(t1, t2, t3);
    }

    /**
     * Returns a Function with 3 arguments applied to the supplied QuadFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param quadFunc Function that accepts 4 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 3 argument being applied to the incoming QuadFunction
     */

    public static <T1, T2, T3, T4, R> Supplier<R> partial4(final T1 t1, final T2 t2, final T3 t3, final T4 t4,
            final QuadFunction<T1, T2, T3, T4, R> quadFunc) {
        return () -> quadFunc.apply(t1, t2, t3, t4);
    }

    /**
     * Returns a Function with 3 arguments applied to the supplied QuadFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param quadFunc Function that accepts 4 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 3 argument being applied to the incoming QuadFunction
     */

    public static <T1, T2, T3, T4, R> Function<T4, R> partial4(final T1 t1, final T2 t2, final T3 t3,
            final QuadFunction<T1, T2, T3, T4, R> quadFunc) {
        return (t4) -> quadFunc.apply(t1, t2, t3, t4);
    }

    /**
     * Returns a BiFunction with 2 arguments applied to the supplied QuadFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param quadFunc Function that accepts 4 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <R> Function generic return type
     * @return BiFunction as a result of 2 arguments being applied to the incoming QuadFunction
     */
    public static <T1, T2, T3, T4, R> BiFunction<T3, T4, R> partial4(final T1 t1, final T2 t2, final QuadFunction<T1, T2, T3, T4, R> quadFunc) {
        return (t3, t4) -> quadFunc.apply(t1, t2, t3, t4);
    }

    /**
     * Returns a BiFunction with 1 argument applied to the supplied QuadFunction
     * @param t1 Generic argument
     * @param quadFunc Function that accepts 4 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <R> Function generic return type
     * @return TriFunction as a result of 1 argument being applied to the incoming QuadFunction
     */

    public static <T1, T2, T3, T4, R> TriFunction<T2, T3, T4, R> partial4(final T1 t1, final QuadFunction<T1, T2, T3, T4, R> quadFunc) {
        return (t2, t3, t4) -> quadFunc.apply(t1, t2, t3, t4);
    }

    /**
     * Returns a Function with 4 arguments applied to the supplied QuintFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param quintFunc Function that accepts 5 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 4 arguments being applied to the incoming QuintFunction
     */

    public static <T1, T2, T3, T4, T5, R> Supplier<R> partial5(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5,
            final QuintFunction<T1, T2, T3, T4, T5, R> quintFunc) {
        return () -> quintFunc.apply(t1, t2, t3, t4, t5);
    }

    /**
     * Returns a Function with 4 arguments applied to the supplied QuintFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param quintFunc Function that accepts 5 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 4 arguments being applied to the incoming QuintFunction
     */

    public static <T1, T2, T3, T4, T5, R> Function<T5, R> partial5(final T1 t1, final T2 t2, final T3 t3, final T4 t4,
            final QuintFunction<T1, T2, T3, T4, T5, R> quintFunc) {
        return (t5) -> quintFunc.apply(t1, t2, t3, t4, t5);
    }

    /**
     * Returns a BiFunction with 3 arguments applied to the supplied QuintFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param quintFunc Function that accepts 5 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <R> Function generic return type
     * @return BiFunction as a result of 3 arguments being applied to the incoming QuintFunction
     */

    public static <T1, T2, T3, T4, T5, R> BiFunction<T4, T5, R> partial5(final T1 t1, final T2 t2, final T3 t3,
            final QuintFunction<T1, T2, T3, T4, T5, R> quintFunc) {
        return (t4, t5) -> quintFunc.apply(t1, t2, t3, t4, t5);
    }

    /**
     * Returns a TriFunction with 2 arguments applied to the supplied QuintFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param quintFunc Function that accepts 5 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <R> Function generic return type
     * @return TriFunction as a result of 2 arguments being applied to the incoming QuintFunction
     */

    public static <T1, T2, T3, T4, T5, R> TriFunction<T3, T4, T5, R> partial5(final T1 t1, final T2 t2,
            final QuintFunction<T1, T2, T3, T4, T5, R> quintFunc) {
        return (t3, t4, t5) -> quintFunc.apply(t1, t2, t3, t4, t5);
    }

    /**
     * Returns a QuadFunction with 1 argument applied to the supplied QuintFunction
     * @param t1 Generic argument
     * @param quintFunc Function that accepts 5 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <R> Function generic return type
     * @return QuadFunction as a result of 1 argument being applied to the incoming QuintFunction
     */

    public static <T1, T2, T3, T4, T5, R> QuadFunction<T2, T3, T4, T5, R> partial5(final T1 t1,
            final QuintFunction<T1, T2, T3, T4, T5, R> quintFunc) {
        return (t2, t3, t4, t5) -> quintFunc.apply(t1, t2, t3, t4, t5);
    }

    /**
     * Returns a Function with 5 arguments applied to the supplied HexFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param hexFunc Function that accepts 6 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 5 arguments being applied to the incoming HexFunction
     */

    public static <T1, T2, T3, T4, T5, T6, R> Supplier<R> partial6(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5, final T6 t6,
            final HexFunction<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return () -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    /**
     * Returns a Function with 5 arguments applied to the supplied HexFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param hexFunc Function that accepts 6 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 5 arguments being applied to the incoming HexFunction
     */

    public static <T1, T2, T3, T4, T5, T6, R> Function<T6, R> partial6(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5,
            final HexFunction<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return (t6) -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    /**
     * Returns a BiFunction with 4 arguments applied to the supplied HexFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param hexFunc Function that accepts 6 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <R> Function generic return type
     * @return BiFunction as a result of 4 arguments being applied to the incoming HexFunction
     */

    public static <T1, T2, T3, T4, T5, T6, R> BiFunction<T5, T6, R> partial6(final T1 t1, final T2 t2, final T3 t3, final T4 t4,
            final HexFunction<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return (t5, t6) -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    /**
     * Returns a TriFunction with 3 arguments applied to the supplied HexFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param hexFunc Function that accepts 6 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <R> Function generic return type
     * @return TriFunction as a result of 3 arguments being applied to the incoming HexFunction
     */

    public static <T1, T2, T3, T4, T5, T6, R> TriFunction<T4, T5, T6, R> partial6(final T1 t1, final T2 t2, final T3 t3,
            final HexFunction<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return (t4, t5, t6) -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    /**
     * Returns a QuadFunction with 2 arguments applied to the supplied HexFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param hexFunc Function that accepts 6 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <R> Function generic return type
     * @return QuadFunction as a result of 2 arguments being applied to the incoming HexFunction
     */

    public static <T1, T2, T3, T4, T5, T6, R> QuadFunction<T3, T4, T5, T6, R> partial6(final T1 t1, final T2 t2,
            final HexFunction<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return (t3, t4, t5, t6) -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    /**
     * Returns a QuintFunction with 1 argument applied to the supplied HexFunction
     * @param t1 Generic argument
     * @param hexFunc Function that accepts 6 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <R> Function generic return type
     * @return QuintFunction as a result of 1 argument being applied to the incoming HexFunction
     */

    public static <T1, T2, T3, T4, T5, T6, R> QuintFunction<T2, T3, T4, T5, T6, R> partial6(final T1 t1,
            final HexFunction<T1, T2, T3, T4, T5, T6, R> hexFunc) {
        return (t2, t3, t4, t5, t6) -> hexFunc.apply(t1, t2, t3, t4, t5, t6);
    }

    /**
     * Returns a Function with 6 arguments applied to the supplied HeptFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param t6 Generic argument
     * @param heptFunc Function that accepts 7 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 6 arguments being applied to the supplied HeptFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, R> Supplier<R> partial7(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5, final T6 t6,
            final T7 t7, final HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return () -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Returns a Function with 6 arguments applied to the supplied HeptFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param t6 Generic argument
     * @param heptFunc Function that accepts 7 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 6 arguments being applied to the supplied HeptFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, R> Function<T7, R> partial7(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5,
            final T6 t6, final HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return (t7) -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Returns a BiFunction with 5 arguments applied to the supplied HeptFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param heptFunc Function that accepts 7 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <R> Function generic return type
     * @return BiFunction as a result of 5 arguments being applied to the supplied HeptFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, R> BiFunction<T6, T7, R> partial7(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5,
            final HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return (t6, t7) -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Returns a TriFunction with 4 arguments applied to the supplied HeptFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param heptFunc Function that accepts 7 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <R> Function generic return type
     * @return TriFunction as a result of 4 arguments being applied to the supplied HeptFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, R> TriFunction<T5, T6, T7, R> partial7(final T1 t1, final T2 t2, final T3 t3, final T4 t4,
            final HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return (t5, t6, t7) -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Returns a QuadFunction with 3 arguments applied to the supplied HeptFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param heptFunc Function that accepts 7 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <R> Function generic return type
     * @return QuadFunction as a result of 3 arguments being applied to the supplied HeptFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, R> QuadFunction<T4, T5, T6, T7, R> partial7(final T1 t1, final T2 t2, final T3 t3,
            final HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return (t4, t5, t6, t7) -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Returns a QuintFunction with 2 arguments applied to the supplied HeptFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param heptFunc Function that accepts 7 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <R> Function generic return type
     * @return QuintFunction as a result of 2 arguments being applied to the supplied HeptFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, R> QuintFunction<T3, T4, T5, T6, T7, R> partial7(final T1 t1, final T2 t2,
            final HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return (t3, t4, t5, t6, t7) -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Returns a HexFunction with 1 argument applied to the supplied HeptFunction
     * @param t1 Generic argument
     * @param heptFunc Function that accepts 7 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <R> Function generic return type
     * @return HexFunction as a result of 1 argument being applied to the supplied HeptFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, R> HexFunction<T2, T3, T4, T5, T6, T7, R> partial7(final T1 t1,
            final HeptFunction<T1, T2, T3, T4, T5, T6, T7, R> heptFunc) {
        return (t2, t3, t4, t5, t6, t7) -> heptFunc.apply(t1, t2, t3, t4, t5, t6, t7);
    }

    /**
     * Returns a Function with 7 arguments applied to the supplied OctFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param t6 Generic argument
     * @param t7 Generic argument
     * @param octFunc Function that accepts 8 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <T8> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 7 arguments being applied to the supplied OctFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Supplier<R> partial8(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5,
            final T6 t6, final T7 t7, final T8 t8, final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return () -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Returns a Function with 7 arguments applied to the supplied OctFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param t6 Generic argument
     * @param t7 Generic argument
     * @param octFunc Function that accepts 8 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <T8> Generic argument type
     * @param <R> Function generic return type
     * @return Function as a result of 7 arguments being applied to the supplied OctFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Function<T8, R> partial8(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5,
            final T6 t6, final T7 t7, final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return (t8) -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Returns a BiFunction with 6 arguments applied to the supplied OctFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param t6 Generic argument
     * @param octFunc Function that accepts 8 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <T8> Generic argument type
     * @param <R> Function generic return type
     * @return BiFunction as a result of 6 arguments being applied to the supplied OctFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> BiFunction<T7, T8, R> partial8(final T1 t1, final T2 t2, final T3 t3, final T4 t4, final T5 t5,
            final T6 t6, final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return (t7, t8) -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Returns a TriFunction with 5 arguments applied to the supplied OctFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param t5 Generic argument
     * @param octFunc Function that accepts 8 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <T8> Generic argument type
     * @param <R> Function generic return type
     * @return TriFunction as a result of 5 arguments being applied to the supplied OctFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> TriFunction<T6, T7, T8, R> partial8(final T1 t1, final T2 t2, final T3 t3, final T4 t4,
            final T5 t5, final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return (t6, t7, t8) -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Returns a QuadFunction with 4 arguments applied to the supplied OctFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param t4 Generic argument
     * @param octFunc Function that accepts 8 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <T8> Generic argument type
     * @param <R> Function generic return type
     * @return QuadFunction as a result of 4 arguments being applied to the supplied OctFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> QuadFunction<T5, T6, T7, T8, R> partial8(final T1 t1, final T2 t2, final T3 t3, final T4 t4,
            final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return (t5, t6, t7, t8) -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Returns a QuintFunction with 3 arguments applied to the supplied OctFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param t3 Generic argument
     * @param octFunc Function that accepts 8 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <T8> Generic argument type
     * @param <R> Function generic return type
     * @return QuintFunction as a result of 3 arguments being applied to the supplied OctFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> QuintFunction<T4, T5, T6, T7, T8, R> partial8(final T1 t1, final T2 t2, final T3 t3,
            final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return (t4, t5, t6, t7, t8) -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Returns a HexFunction with 2 arguments applied to the supplied OctFunction
     * @param t1 Generic argument
     * @param t2 Generic argument
     * @param octFunc Function that accepts 8 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <T8> Generic argument type
     * @param <R> Function generic return type
     * @return HexFunction as a result of 2 arguments being applied to the supplied OctFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> HexFunction<T3, T4, T5, T6, T7, T8, R> partial8(final T1 t1, final T2 t2,
            final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return (t3, t4, t5, t6, t7, t8) -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

    /**
     * Returns a HeptFunction with 1 argument applied to the supplied OctFunction
     * @param t1 Generic argument
     * @param octFunc Function that accepts 8 parameters
     * @param <T1> Generic argument type
     * @param <T2> Generic argument type
     * @param <T3> Generic argument type
     * @param <T4> Generic argument type
     * @param <T5> Generic argument type
     * @param <T6> Generic argument type
     * @param <T7> Generic argument type
     * @param <T8> Generic argument type
     * @param <R> Function generic return type
     * @return HeptFunction as a result of 1 arguments being applied to the supplied OctFunction
     */

    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> HeptFunction<T2, T3, T4, T5, T6, T7, T8, R> partial8(final T1 t1,
            final OctFunction<T1, T2, T3, T4, T5, T6, T7, T8, R> octFunc) {
        return (t2, t3, t4, t5, t6, t7, t8) -> octFunc.apply(t1, t2, t3, t4, t5, t6, t7, t8);
    }

}