package cyclops.function;

import java.util.function.Predicate;

/**
 * Lambda type inferencing helper / curried function creation helper
 * 
 * @author johnmcclean
 *
 */
public class Lambda {

    public static <T> Iterable<T> it(Iterable<T> it){
        return it;
    }
    /**
     * E.g. toNested use a supplier toNested embed additional code inisde a ternary operator
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
     * @param supplier Lambda / method toNested assign type of Supplier toNested
     * @return Supplier
     */
    public static <T> Fn0<T> s(final Fn0<T> supplier) {
        return supplier;
    }
    /**
     * E.g. toNested use a supplier toNested embed additional code inisde a ternary operator
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
     * @param supplier Lambda / method toNested assign type of Supplier toNested
     * @return Supplier
     */
    public static <T> Fn0<T> λ(final Fn0<T> supplier) {
        return supplier;
    }
    public static <T> Fn0.SupplierKind<T> λK(final Fn0.SupplierKind<T> supplier) {
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
    public static <T1, R> Fn1<T1, R> λ(final Fn1<T1, R> func) {
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
    public static <T1, R> Fn1<T1, R> l1(final Fn1<T1, R> func) {
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
    public static <T1, T2, R> Fn2<T1,T2, R> λ(final Fn2<T1,T2, R> biFunc) {
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
    public static <T1, T2, T3,R> Fn3<T1,T2,T3, R> λ(final Fn3<T1,T2,T3, R> triFunc) {
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
    public static <T1, T2, T3, T4,R> Fn4<T1,T2,T3, T4,R> λ(final Fn4<T1,T2,T3,T4, R> quadFunc) {
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
    public static <T1, T2, T3, T4, T5,R> Fn5<T1,T2,T3, T4, T5,R> λ(final Fn5<T1,T2,T3,T4,T5, R> quintFunc) {
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
    public static <T1, T2, T3, T4, T5, T6,R> Fn6<T1,T2,T3, T4, T5,T6,R> λ(final Fn6<T1,T2,T3,T4,T5,T6, R> func6) {
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
    public static <T1, T2, T3, T4, T5, T6,T7,R> Fn7<T1,T2,T3, T4, T5,T6,T7,R> λ(final Fn7<T1,T2,T3,T4,T5,T6,T7, R> func7) {
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
    public static <T1, T2, T3, T4, T5, T6,T7,T8,R> Fn8<T1,T2,T3, T4, T5,T6,T7,T8,R> λ(final Fn8<T1,T2,T3,T4,T5,T6,T7,T8, R> func8) {
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
    public static <T1, T2, R> Fn1<T1, Fn1<T2, R>> l2(final Fn1<T1, Fn1<T2, R>> biFunc) {
        return biFunc;
    }
    public static <T1, T2, R> Fn1<? super T1, ? extends Fn1<? super T2, ? extends R>> v2(final Fn1<? super T1, Fn1<? super T2, ? extends R>> biFunc) {
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
    public static <T1, T2, T3, R> Fn1<T1, Fn1<T2, Fn1<T3, R>>> l3(final Fn1<T1, Fn1<T2, Fn1<T3, R>>> triFunc) {
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
    public static <T1, T2, T3, T4, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, R>>>> l4(
            final Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, R>>>> quadFunc) {
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
    public static <T1, T2, T3, T4, T5, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, R>>>>> l5(
            final Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, R>>>>> pentFunc) {
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
    public static <T1, T2, T3, T4, T5, T6, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, R>>>>>> l6(
            final Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, R>>>>>> hexFunc) {
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
    public static <T1, T2, T3, T4, T5, T6, T7, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, Fn1<T7, R>>>>>>> l7(
            final Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, Fn1<T7, R>>>>>>> heptFunc) {
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
    public static <T1, T2, T3, T4, T5, T6, T7, T8, R> Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, Fn1<T7, Fn1<T8, R>>>>>>>> l8(
            final Fn1<T1, Fn1<T2, Fn1<T3, Fn1<T4, Fn1<T5, Fn1<T6, Fn1<T7, Fn1<T8, R>>>>>>>> octFunc) {
        return octFunc;
    }
}
