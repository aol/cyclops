package com.oath.cyclops.hkt;

import java.util.function.BiFunction;
import java.util.function.Function;



/**
 * Higher Kinded Type - a core type (e.g. a List) and a data type of the elements within the List (e.g. Integers).
 *
 *
 * @author johnmcclean
 *
 * @param <T1> Core type
 * @param <T2> Data type of elements in Core Type
 */
public interface Higher<T1,T2> extends Convert<Higher<T1,T2>>{

    /**
     * Apply the provided BiFunction passing this as the second parameter
     *
     * This allows a fluent api without narrowing or unwrapping simulated Higher Kinded Types
     * <pre>
     * {@code
     *  Functor<ListType.µ> f = TypeClasses.General
    .<ListType.µ,List<?>>functor(ListType::narrow,(list,fn)->Seq.fromIterable(list).map(fn));

    List<Integer> mapped2 = f.map(a->a+1, ListType.widen(Arrays.asList(1,2,3)))
    .then_(f::transform,λ(this::mult3))
    .then_(f::transform,λ(this::add2))
    .convert(ListType::narrow);
     *
     * }
     * </pre>
     *
     * @param biFn BiFunction to execute
     * @param param 1st parameter to pass to BiFunction
     * @return Result of executing the provided BiFunction
     */
    default <T3,R> Higher<T1,R> applyHKT_(BiFunction<? super T3,? super Higher<T1,T2>,? extends Higher<T1,R>> biFn, T3 param ){
        return biFn.apply(param,this);
    }
    default <R> Higher<T1,R> applyHKT(Function<? super Higher<T1,T2>,? extends Higher<T1,R>> fn){
        return fn.apply(this);
    }
    /**
     * Apply the provided BiFunction passing this as the first parameter
     * This allows a fluent api without narrowing or unwrapping simulated Higher Kinded Types
     *
     * @param biFn BiFunction to execute
     * @param param 2nd parameter to pass to BiFunction
     * @return Result of executing the provided BiFunction
     */
    default <T3,R> Higher<T1,R> applyHKT(T3 param, BiFunction<? super Higher<T1,T2>,? super T3,? extends Higher<T1,R>> biFn ){
        return biFn.apply(this,param);
    }



}
