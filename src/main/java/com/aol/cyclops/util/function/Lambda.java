package com.aol.cyclops.util.function;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jooq.lambda.function.Function6;
import org.jooq.lambda.function.Function7;
import org.jooq.lambda.function.Function8;

/**
 * Lambda type inferencing helper / curried function creation helper
 * 
 * @author johnmcclean
 *
 */
public class Lambda {
    /**
     * E.g. to use a supplier to embed additional code inisde a ternary operator
     * 
     * <pre>
     * {@code 
     * return pos >= values.length ? tuple(true, split) : Lambda.s(() -> {
    			action.accept(values[pos++]);
    			return tuple(true, this);
    		}).get();
     * 
     * }
     * </pre>
     * 
     * @param supplier Lambda / method to assign type of Supplier to  
     * @return Supplier
     */
    public static <T> F0<T> s(final F0<T> supplier) {
        return supplier;
    }
    /**
     * E.g. to use a supplier to embed additional code inisde a ternary operator
     * 
     * <pre>
     * {@code 
     * return pos >= values.length ? tuple(true, split) : Lambda.s(() -> {
                action.accept(values[pos++]);
                return tuple(true, this);
            }).get();
     * 
     * }
     * </pre>
     * 
     * @param supplier Lambda / method to assign type of Supplier to  
     * @return Supplier
     */
    public static <T> F0<T> λ(final F0<T> supplier) {
        return supplier;
    }
    public static <T> Predicate<T> λ(final Predicate<T> pred) {
        return  pred;
    }
    public static <T> Predicate<T> p(final Predicate<T> p) {
        return  p;
    }
    /**
     * Alias for l1
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer i)->"hello")
     * }</pre>
     * @param func
     * @return supplied function
     */
    public static <T1, R> F1<T1, R> λ(final F1<T1, R> func) {
        return func;
    }
    /**
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     * 		val fn  = l1((Integer i)->"hello")
     * }</pre>
     * @param func
     * @return supplied function
     */
    public static <T1, R> F1<T1, R> l1(final F1<T1, R> func) {
        return func;
    }
    /**
     * Create a curried function with arity of 2
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param biFunc
     * @return supplied function
     */
    public static <T1, T2, R> F2<T1,T2, R> λ(final F2<T1,T2, R> biFunc) {
        return biFunc;
    }
    /**
     * Create a curried function with arity of 3
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param triFunc
     * @return supplied function
     */
    public static <T1, T2, T3,R> F3<T1,T2,T3, R> λ(final F3<T1,T2,T3, R> triFunc) {
        return triFunc;
    }
    /**
     * Create a curried function with arity of 4
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param quadFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4,R> F4<T1,T2,T3, T4,R> λ(final F4<T1,T2,T3,T4, R> quadFunc) {
        return quadFunc;
    }
    /**
     * Create a curried function with arity of 5
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param quintFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5,R> F5<T1,T2,T3, T4, T5,R> λ(final F5<T1,T2,T3,T4,T5, R> quintFunc) {
        return quintFunc;
    }
    /**
     * Create a curried function with arity of 6
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param func6
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, T6,R> F6<T1,T2,T3, T4, T5,T6,R> λ(final F6<T1,T2,T3,T4,T5,T6, R> func6) {
        return func6;
    }
    /**
     * Create a curried function with arity of 7
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param quadFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, T6,T7,R> F7<T1,T2,T3, T4, T5,T6,T7,R> λ(final F7<T1,T2,T3,T4,T5,T6,T7, R> func7) {
        return func7;
    }
    /**
     * Create a curried function with arity of 8
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     *      val fn  = λ((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param quadFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, T6,T7,T8,R> F8<T1,T2,T3, T4, T5,T6,T7,T8,R> λ(final F8<T1,T2,T3,T4,T5,T6,T7,T8, R> func8) {
        return func8;
    }
    /**
     * Create a curried function with arity of 2
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     * 		val fn  = l3((Integer a)-> (Integer b)-> a+b+)
     * }</pre>
     * @param biFunc
     * @return supplied function
     */
    public static <T1, T2, R> F1<T1, F1<T2, R>> l2(final F1<T1, F1<T2, R>> biFunc) {
        return biFunc;
    }
    public static <T1, T2, R> F1<? super T1, ? extends F1<? super T2, ? extends R>> v2(final F1<? super T1, F1<? super T2, ? extends R>> biFunc) {
        return biFunc;
    }

    /**
     * Create a curried function with arity of 3
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     * 		val fn  = l3((Integer a)-> (Integer b)->(Integer c) -> a+b+c)
     * }</pre>
     * @param triFunc
     * @return supplied function
     */
    public static <T1, T2, T3, R> F1<T1, F1<T2, F1<T3, R>>> l3(final F1<T1, F1<T2, F1<T3, R>>> triFunc) {
        return triFunc;
    }

    /**
     * Create a curried function with arity of 4
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> a+b+c+d)
     * }</pre>
     * @param quadFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, R> F1<T1, F1<T2, F1<T3, F1<T4, R>>>> l4(
            final F1<T1, F1<T2, F1<T3, F1<T4, R>>>> quadFunc) {
        return quadFunc;
    }

    /**
     * Create a curried function with arity of 5
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> a+b+c+d+e)
     * }</pre>
     * @param pentFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, R> F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, R>>>>> l5(
            final F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, R>>>>> pentFunc) {
        return pentFunc;
    }

    /**
     * Create a curried function with arity of 6
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)-> a+b+c+d+e+f)
     * }</pre>
     * @param hexFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, T6, R> F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, R>>>>>> l6(
            final F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, R>>>>>> hexFunc) {
        return hexFunc;
    }

    /**
     * Create a curried function with arity of 7
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)->(Integer g) -> a+b+c+d+e+f+g)
     * }</pre>
     * @param heptFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, T6, T7, R> F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, F1<T7, R>>>>>>> l7(
            final F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, F1<T7, R>>>>>>> heptFunc) {
        return heptFunc;
    }

    /**
     * Create a curried function with arity of 8
     * 
     * e.g. with Lombok val 
     * 
     * <pre>{@code
     * 		val fn  = l4((Integer a)-> (Integer b)->(Integer c) -> (Integer d) -> (Integer e) -> (Integer f)->(Integer g) -> (Integer h) ->a+b+c+d+e+f+g+h)
     * }</pre>
     * @param octFunc
     * @return supplied function
     */
    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, F1<T7, F1<T8, R>>>>>>>> l8(
            final F1<T1, F1<T2, F1<T3, F1<T4, F1<T5, F1<T6, F1<T7, F1<T8, R>>>>>>>> octFunc) {
        return octFunc;
    }
}
